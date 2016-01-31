package com.pkuhelper.ui.hole.impl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pkuhelper.R;
import com.pkuhelper.lib.MyBitmapFactory;
import com.pkuhelper.manager.ImageManager;
import com.pkuhelper.manager.MediaPathManager;
import com.pkuhelper.model.Callback;
import com.pkuhelper.presenter.HoleCommentPresenter;
import com.pkuhelper.presenter.HolePresenter;
import com.pkuhelper.ui.hole.IHolePostUI;

import java.io.IOException;


public class HolePostFragment extends DialogFragment implements IHolePostUI {
    final int TYPE_TEXT=0;
    final int TYPE_IMAGE=1;
    final int TYPE_AUDIO=2;
    final int START_TYPE_HOLE=0;
    final int START_TYPE_COMMENT=1;
    final int START_TYPE_REPORT=2;
    final int START_TYPE_SEARCH=3;

    private HolePresenter mHolePresenter;
    private HoleCommentPresenter mHoleCommentPresenter;

    private OnFragmentInteractionListener mListener;

    private ImageButton btnPost;
    private ImageButton btnImg;
    private ImageButton btnAudio;
    private TextView tvHolePost;
    private EditText etContent;
    private ImageView imgPreview;
    private View buttons;
    private String uri;
    private int type=TYPE_TEXT;
    private int startType = -1;
    private int pid = 0;
    private boolean isReply;
    private int replyCid;
    Bitmap bitmap;
    private Context context;
    private View rootView;

    public HolePostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Bundle bundle = getArguments();
        String startTypeString = bundle.getString("start-type");
        Log.d("startType:", startTypeString);

        if (startTypeString == null){
            Log.e("HolePostFragment:","start type equals to null");
        }
        else if (startTypeString.equals("comment")){
            startType = START_TYPE_COMMENT;
        }
        else if (startTypeString.equals("hole")){
            startType = START_TYPE_HOLE;
        }
        else if (startTypeString.equals("report")){
            startType = START_TYPE_REPORT;
        }
        else if (startTypeString.equals("search")){
            startType = START_TYPE_SEARCH;
        }
        else{
        }


        switch (startType){
            case START_TYPE_COMMENT:
                pid = bundle.getInt("pid");
                if (isReply = bundle.getBoolean("isReply",false)){
                    replyCid = bundle.getInt("cid");
                }
                mHoleCommentPresenter = new HoleCommentPresenter(getContext());
                break;
            case START_TYPE_HOLE:
                mHolePresenter =  new HolePresenter(getContext());
                break;
            case START_TYPE_REPORT:
                pid = bundle.getInt("pid");
                mHoleCommentPresenter = new HoleCommentPresenter(getContext());
                break;
            case START_TYPE_SEARCH:
                mHolePresenter =  new HolePresenter(getContext());
                default:
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_hole_post, container, false);

        findWidgets(view);
        /*
        * 设置title等
        * */
        switch (startType){
            case START_TYPE_COMMENT:
                tvHolePost.setText("发布评论");
                view.findViewById(R.id.linearLayout_hole_post_button).setVisibility(View.GONE);
                //设置回复字符串
                if (isReply){
                    tvHolePost.setText("回复评论");
                    String text = "Re #"+replyCid+": ";
                    etContent.setText(text);
                    etContent.setSelection(text.length());
                }
                break;
            case START_TYPE_HOLE:
                tvHolePost.setText("发布新树洞");
                view.findViewById(R.id.linearLayout_hole_post_button).setVisibility(View.VISIBLE);
                setupUploadMedia();
                break;
            case START_TYPE_REPORT:
                tvHolePost.setText("举报树洞");
                view.findViewById(R.id.linearLayout_hole_post_button).setVisibility(View.GONE);
                break;
            case START_TYPE_SEARCH:
                tvHolePost.setText("搜索树洞");
                view.findViewById(R.id.linearLayout_hole_post_button).setVisibility(View.GONE);
            default:
        }

        /*
        * 点击发送图标的反应
        * */
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etContent.getText().toString();

                switch (startType){
                    case START_TYPE_COMMENT:
                        postComment(text);
                        break;
                    case START_TYPE_HOLE:
                        postHole(text);
                        break;
                    case START_TYPE_REPORT:
                        postReport(text);
                        break;
                    case START_TYPE_SEARCH:
                        postSearch(text);
                        break;
                    default:
                        break;
                }

                dismiss();
            }

        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 结果码不等于取消时候
        if (resultCode != Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    uri = data.getData().toString();
                    imgPreview.setVisibility(View.VISIBLE);
                    etContent.setHeight(100);
                    buttons.refreshDrawableState();
                    etContent.refreshDrawableState();
                    (new ImageManager(getContext())).displayBigImage(uri, imgPreview);


                    bitmap = MyBitmapFactory.getCompressedBitmap(MediaPathManager.getPath(getContext(), Uri.parse(uri)),5);
            }
        super.onActivityResult(requestCode, resultCode, data);
    }}

    @Override
    public void onStart()
    {
        super.onStart();

        /*
        * 调整dialog大小
        * */
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        getDialog().getWindow().setLayout( dm.widthPixels, getDialog().getWindow().getAttributes().height );
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void changeToImage() {

    }

    @Override
    public void changeToAudio() {

    }

    @Override
    public Bundle prepareData() {
        return null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private void findWidgets(View view){
        tvHolePost = (TextView) view.findViewById(R.id.tv_hole_post);
        btnPost = (ImageButton) view.findViewById(R.id.btn_hole_post);
        btnImg = (ImageButton) view.findViewById(R.id.btn_hole_post_image);
        btnAudio =(ImageButton) view.findViewById(R.id.btn_hole_post_audio);
        imgPreview = (ImageView) view.findViewById(R.id.img_hole_post_preview);
        etContent = (EditText) view.findViewById(R.id.et_hole_post);
        buttons = view.findViewById(R.id.linearLayout_hole_post_button);
    }

    private void setupUploadMedia(){
        btnImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);//ACTION_OPEN_DOCUMENT
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/jpeg");
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    startActivityForResult(intent, 0);
                } else {
                    startActivityForResult(intent, 0);
                }
                type = TYPE_IMAGE;
            }
        });
    }

    private void postComment(String text){
        context = getActivity();
        rootView = ((HoleCommentActivity)context).findViewById(R.id.cv_hole_comment_card);

        Log.d("view", rootView.toString());

        Callback callback = new Callback() {
            @Override
            public void onFinished(int code, Object data) {
                Log.d("code:", code + "");
                if (code == 0)
                    Snackbar.make(rootView,"发送成功",Snackbar.LENGTH_LONG).show();
                else
                    Snackbar.make(rootView, "发送失败", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onError(String msg) {
                Snackbar.make(rootView, "发送失败", Snackbar.LENGTH_LONG).show();
            }
        };

        mHoleCommentPresenter.reply(pid, text, callback);
    }

    private void postHole(String text){

        context = getActivity();
        rootView = ((HoleActivity)context).findViewById(R.id.nav_view);
        Bundle bundle=new Bundle();
        switch (type) {
            case TYPE_TEXT:
                bundle.putString("type","text");
                bundle.putString("text", text);
                break;
            case TYPE_AUDIO:
                bundle.putString("type","audio");
                bundle.putString("uri",uri);
                break;
            case TYPE_IMAGE:

                Log.d("bitmap size:",""+bitmap.getByteCount());
                byte[] bts = MyBitmapFactory.bitmapToArray(bitmap);
                String data = Base64.encodeToString(bts, Base64.DEFAULT);
                bundle.putString("data",data);
                bundle.putString("type","image");
                bundle.putString("uri",uri);
                bundle.putString("text",text);
        }
        try {
            Callback callback = new Callback() {
                @Override
                public void onFinished(int code, Object data) {
                    Log.d("code:", code + "");
                    if (code == 0)
                        Snackbar.make(rootView,"发送成功",Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(rootView, "发送失败", Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void onError(String msg) {
                    Snackbar.make(rootView, "发送失败", Snackbar.LENGTH_LONG).show();
                }
            };
            mHolePresenter.post(bundle,callback);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void postReport(String text){
        context = getActivity();
        Callback simpleCallback = new Callback<Void>() {
            @Override
            public void onFinished(int code, Void data) {
                Log.d("code:",code+"");
                if (code == 0)
                    Toast.makeText(context,"举报成功",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context,"举报失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String msg) {
                Log.d("error",msg);
                Toast.makeText(getActivity(),"举报失败",Toast.LENGTH_SHORT).show();
            }
        };

        mHoleCommentPresenter.report(pid, text, simpleCallback);
    }

    private void postSearch(String text){
        mHolePresenter.search(text);
    }
}

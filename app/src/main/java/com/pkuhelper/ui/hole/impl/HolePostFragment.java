package com.pkuhelper.ui.hole.impl;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.text.Layout;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pkuhelper.R;
import com.pkuhelper.lib.MyBitmapFactory;
import com.pkuhelper.manager.ImageManager;
import com.pkuhelper.manager.MediaPathManager;
import com.pkuhelper.model.Callback;
import com.pkuhelper.model.impl.PkuHoleMod;
import com.pkuhelper.presenter.HolePresenter;
import com.pkuhelper.presenter.IHolePresenter;
import com.pkuhelper.ui.hole.IHolePostUI;
import com.squareup.picasso.Picasso;

import java.io.IOException;


public class HolePostFragment extends DialogFragment implements IHolePostUI {
    final int TYPE_TEXT=0;
    final int TYPE_IMAGE=1;
    final int TYPE_AUDIO=2;

    private HolePresenter mHolePresenter;

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
    private String startType;
    private int pid = 0;
    private boolean isReply;
    private int replyCid;
    Bitmap bitmap;


    public HolePostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Bundle bundle = getArguments();
        startType = bundle.getString("start-type");
        Log.d("startType:", startType);
        if (startType.equals("comment")){
            pid = bundle.getInt("pid");
        }
        else if (startType.equals("hole")){
            mHolePresenter =  new HolePresenter(getContext());
        }
        if (isReply = bundle.getBoolean("isReply",false)){
            replyCid = bundle.getInt("cid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_hole_post, container, false);

        tvHolePost = (TextView) view.findViewById(R.id.tv_hole_post);
        btnPost = (ImageButton) view.findViewById(R.id.btn_hole_post);
        btnImg = (ImageButton) view.findViewById(R.id.btn_hole_post_image);
        btnAudio =(ImageButton) view.findViewById(R.id.btn_hole_post_audio);
        imgPreview = (ImageView) view.findViewById(R.id.img_hole_post_preview);
        etContent = (EditText) view.findViewById(R.id.et_hole_post);
        buttons = view.findViewById(R.id.linearLayout_hole_post_button);

        if (startType.equals("comment")){
            tvHolePost.setText("发布评论");
            view.findViewById(R.id.linearLayout_hole_post_button).setVisibility(View.GONE);
        }
        else if (startType.equals("hole")){
            tvHolePost.setText("发布新树洞");
        }
        //设置回复字符串
        if (isReply){
            tvHolePost.setText("回复评论");
            String text = "Re #"+replyCid+": ";
            etContent.setText(text);
            etContent.setSelection(text.length());
        }

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
                type=TYPE_IMAGE;
            }
        });



        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etContent.getText().toString();

                if (startType.equals("comment")){
                    Callback simpleCallback = new Callback<Void>() {
                        @Override
                        public void onFinished(int code, Void data) {
                            Log.d("success code:",""+code);
                        }

                        @Override
                        public void onError(String msg) {
                            Log.d("error",msg);
                        }
                    };

                    new PkuHoleMod(getContext()).reply(pid, text, simpleCallback);
                }
                else{
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
                        mHolePresenter.post(bundle);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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


}

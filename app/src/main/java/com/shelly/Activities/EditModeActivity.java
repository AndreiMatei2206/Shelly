package com.shelly.Activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shelly.Models.EntryContent;
import com.shelly.Models.TextTransformationModel;
import com.shelly.R;
import com.shelly.Adapters.TextTransformListAdapter;
import com.shelly.Utils.TextTransformationUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class EditModeActivity extends AppCompatActivity {

    //Views
    private ImageButton mExitEditMode;
    private ImageButton mTextFormatBtn;
    private ImageButton mSettingsEditMode;
    private ImageButton mAddResource;
    private ScrollView mScrollView;
    private EditText mEntryContentET;
    private RecyclerView mTextTypeRV;
    private RecyclerView mTextStyleRV;
    private TextTransformListAdapter mTextTypeAdapter;
    private TextTransformListAdapter mTextStyleAdapter;
    private ConstraintLayout mTextFormatMenu;
    private ImageButton mCloseTextFormatMenu;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRefDatabase;

    //Variables
    private List<TextTransformationModel> mTextTypeList;
    private List<TextTransformationModel> mTextStyleList;
    private List<String> mAllTags;
    private EntryContent mEntryContent;
    boolean mTextFormatMenuOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mode);

        //Binding views
        mExitEditMode = findViewById(R.id.ExitEditModeImageButton);
        mTextFormatBtn = findViewById(R.id.TextFormatImageButton);
        mSettingsEditMode = findViewById(R.id.EditModeSettingsImageButton);
        mAddResource = findViewById(R.id.EditModeAddImageButton);
        mScrollView = findViewById(R.id.EditModeScrollView);
        mEntryContentET = findViewById(R.id.EditModeContentEditText);
        mTextTypeRV = findViewById(R.id.TextTypeRecyclerView);
        mTextStyleRV = findViewById(R.id.TextStyleRecyclerView);
        mTextFormatMenu = findViewById(R.id.TextFormatMenu);
        mCloseTextFormatMenu = findViewById(R.id.CloseTextFormatImageButton);

        //Firebase Binding
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mRefDatabase = mDatabase.getReference();

        //Variable Binding
        mTextTypeList = new ArrayList<>();
        mTextStyleList = new ArrayList<>();
        mAllTags = new ArrayList<>();
        mEntryContent = new EntryContent();
        mEntryContent.setTags(new ArrayList<String>());
        mEntryContent.setText(new StringBuilder());

        //Implementing Functionalities
        initializeTextTransformation();
        mTextTypeAdapter = new TextTransformListAdapter(mTextTypeList, this, 0, mEntryContent);
        mTextStyleAdapter = new TextTransformListAdapter(mTextStyleList, this, 1, mEntryContent);
        mTextTypeRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mTextStyleRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mTextTypeRV.setAdapter(mTextTypeAdapter);
        mTextStyleRV.setAdapter(mTextStyleAdapter);

        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /*Log.e("testing", charSequence.toString());
                Log.e("testing", "" + i);
                Log.e("testing", "" + i1);
                Log.e("testing", "" + i2);
                Log.e("new position", "" + TextTransformationUtils.findNewStartingPosition(mEntryContent.getText(), i, mAllTags));*/
                int position = TextTransformationUtils.findNewStartingPosition(mEntryContent.getText(), start, mAllTags);
                int cursorEndPosition = mEntryContentET.getSelectionEnd();
                if(count > before) {
                    mEntryContent.setText(mEntryContent.getText().insert(position + before, s.subSequence(start + before, start + count).toString()));
                } else {
                    mEntryContent.setText(mEntryContent.getText().replace(position + count, position + before, ""));
                }
                Log.e("Content", mEntryContent.getText().toString());
                mEntryContentET.removeTextChangedListener(this);
                mEntryContentET.setText(Html.fromHtml(mEntryContent.getText().toString()));
                mEntryContentET.addTextChangedListener(this);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        mEntryContentET.addTextChangedListener(textWatcher);

        mExitEditMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EditModeActivity.this, EntryActivity.class);
                startActivity(i);
                finish();
            }
        });
        mCloseTextFormatMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextFormatMenuOpen = false;
                Animation slideOutBottom = AnimationUtils.loadAnimation(EditModeActivity.this, R.anim.slide_out_bottom);
                mTextFormatMenu.startAnimation(slideOutBottom);

                slideOutBottom.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mTextFormatMenu.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });

        mTextFormatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextFormatMenuOpen = !mTextFormatMenuOpen;
                if(mTextFormatMenuOpen) {
                    Animation slideInBottom = AnimationUtils.loadAnimation(EditModeActivity.this, R.anim.slide_in_bottom);
                    mTextFormatMenu.startAnimation(slideInBottom);

                    slideInBottom.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            mTextFormatMenu.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                } else {
                    Animation slideOutBottom = AnimationUtils.loadAnimation(EditModeActivity.this, R.anim.slide_out_bottom);
                    mTextFormatMenu.startAnimation(slideOutBottom);

                    slideOutBottom.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mTextFormatMenu.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
        });

    }

    private void initializeTextTransformation() {

        Typeface SegoeUI = ResourcesCompat.getFont(this, R.font.segoe_ui);
        Typeface SegoeUISemibold = ResourcesCompat.getFont(this, R.font.segoe_semibold);

        TextTransformationModel textTransformationModel = new TextTransformationModel();
        textTransformationModel.setFieldValue("Title");
        textTransformationModel.setAssignedTag("h1");
        textTransformationModel.setTextFont(SegoeUISemibold);
        textTransformationModel.setSelected(false);
        mTextTypeList.add(textTransformationModel);
        mAllTags.add("h1");

        textTransformationModel = new TextTransformationModel();
        textTransformationModel.setFieldValue("<b>Heading<b>");
        textTransformationModel.setAssignedTag("h3");
        textTransformationModel.setTextFont(SegoeUI);
        textTransformationModel.setSelected(false);
        mTextTypeList.add(textTransformationModel);
        mAllTags.add("h3");

        textTransformationModel = new TextTransformationModel();
        textTransformationModel.setFieldValue("Body");
        textTransformationModel.setAssignedTag("p");
        textTransformationModel.setTextFont(SegoeUI);
        textTransformationModel.setSelected(true);
        mTextTypeList.add(textTransformationModel);
        mEntryContent.getTags().add(textTransformationModel.getAssignedTag());
        mAllTags.add("p");

        textTransformationModel = new TextTransformationModel();
        textTransformationModel.setFieldValue("<b>B<b>");
        textTransformationModel.setAssignedTag("b");
        textTransformationModel.setTextFont(SegoeUI);
        textTransformationModel.setSelected(false);
        mTextStyleList.add(textTransformationModel);
        mAllTags.add("b");

        textTransformationModel = new TextTransformationModel();
        textTransformationModel.setFieldValue("<i>I<i>");
        textTransformationModel.setAssignedTag("i");
        textTransformationModel.setTextFont(SegoeUI);
        textTransformationModel.setSelected(false);
        mTextStyleList.add(textTransformationModel);
        mAllTags.add("i");

        textTransformationModel = new TextTransformationModel();
        textTransformationModel.setFieldValue("<u>U<u>");
        textTransformationModel.setAssignedTag("u");
        textTransformationModel.setTextFont(SegoeUI);
        textTransformationModel.setSelected(false);
        mTextStyleList.add(textTransformationModel);
        mAllTags.add("u");

        for(int i = mAllTags.size() - 1; i>= 0; i--) {
            mAllTags.add("</" + mAllTags.get(i) + ">");
            mAllTags.set(i, "<" + mAllTags.get(i) + ">");
        }

    }
}

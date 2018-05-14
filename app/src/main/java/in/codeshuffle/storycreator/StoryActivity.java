package in.codeshuffle.storycreator;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;

public class StoryActivity extends AppCompatActivity {

    int offX, offY;
    int id = 0;

    //main container
    RelativeLayout container;
    View deleteLayout;

    //container controls
    ImageButton addNewTextView, closeScreen, addSticker, addBrushPath;

    //Adding new text
    boolean addingTextView;
    View textEditLayout;
    ImageButton textStyle, textGravity;
    Button doneEditing;
    EditText textToEdit;
    TextView newTextView;
    TextView viewBeingEdited;
    SeekBar textSizeChanger;
    int[] gravity = {Gravity.LEFT, Gravity.CENTER, Gravity.RIGHT};
    int[] gravityAsset = {R.drawable.left, R.drawable.center, R.drawable.right};
    int curGravity = 0;
    int[] textWeight = {Typeface.BOLD, Typeface.ITALIC};
    int[] textWeightAsset = {R.drawable.bold, R.drawable.italic};
    int curTextWeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        Bitmap bmp = null;
        String filename = getIntent().getStringExtra("image");
        try {
            byte[] byteArray = getIntent().getByteArrayExtra("image");
            bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            findViewById(R.id.imgContainer).setBackgroundDrawable(new BitmapDrawable(bmp));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //make full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //get all views
        //container
        container = findViewById(R.id.container);
        textEditLayout = findViewById(R.id.textEditLayout);
        deleteLayout = findViewById(R.id.deleteItemLayout);

        //container controls
        addNewTextView = findViewById(R.id.addNewTextView);
        closeScreen = findViewById(R.id.closeEditor);
        addSticker = findViewById(R.id.addSticker);
        addBrushPath = findViewById(R.id.addBrushPath);
        textSizeChanger = findViewById(R.id.textSizeChanger);

        //text add
        doneEditing = findViewById(R.id.doneEditingText);
        textToEdit = findViewById(R.id.textToEdit);
        textGravity = findViewById(R.id.textGravity);
        textStyle = findViewById(R.id.textStyle);

        //init visibilities
        container.setVisibility(View.VISIBLE);
        textEditLayout.setVisibility(View.GONE);

        //init Setup
        initTextViewControls();


        //set drag listeners
        container.setOnDragListener(new layoutDragListener());
        deleteLayout.setOnDragListener(new layoutDragListener());
    }


    //textview init setup
    private void initTextViewControls() {
        textEditLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!textToEdit.hasFocus())
                    textToEdit.requestFocus();
                openKeyboard();
                return false;
            }
        });
        //done adding editing textview
        doneEditing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAndAddTextView();
                container.setVisibility(View.VISIBLE);
                textEditLayout.setVisibility(View.GONE);
                closeKeyboard();
            }
        });

        //add new text
        addNewTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addingTextView = true;
                addEditTextView();
            }
        });

        //text size changer
        textSizeChanger.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textToEdit.setTextSize(i);
                if (!addingTextView)
                    viewBeingEdited.setTextSize(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //gravity
        textGravity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!addingTextView)
                    viewBeingEdited.setGravity(gravity[curGravity]);
                textToEdit.setGravity(gravity[curGravity]);
                textGravity.setImageResource(gravityAsset[curGravity]);
                curGravity = (curGravity + 1) % 3;
            }
        });

        //text style
        textStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!addingTextView)
                    viewBeingEdited.setTypeface(null, textWeight[curTextWeight]);
                textToEdit.setTypeface(null, textWeight[curTextWeight]);
                textStyle.setImageResource(textWeightAsset[curTextWeight]);
                curTextWeight = (curTextWeight + 1) % 2;
            }
        });
    }


    //0=add 1=edit
    private void addEditTextView() {
        container.setVisibility(View.GONE);
        textEditLayout.setVisibility(View.VISIBLE);
        if (addingTextView) {
            openKeyboard();
            textToEdit.setText("");
            textToEdit.requestFocus();
        } else {
            String text = viewBeingEdited.getText().toString();
            textToEdit.setText(text);
            textToEdit.setSelection(text.length());
        }
    }

    private void getAndAddTextView() {
        newTextView = addingTextView ? new TextView(StoryActivity.this) : viewBeingEdited;
        if (addingTextView) {
            newTextView.setId(id++);
            newTextView.setTextSize(textSizeChanger.getProgress());
            newTextView.setTypeface(null, Typeface.BOLD);
            newTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    viewBeingEdited = (TextView) view;
                    addingTextView = false;
                    addEditTextView();
                }
            });
            newTextView.setOnTouchListener(new ItemsTouchListener());
            container.addView(newTextView);
        }
        if (textToEdit.getText().length() > 0) {
            newTextView.setText(textToEdit.getText().toString());
        }

    }

    //hide all container elements except items
    public void toggleMainControlVisibility(int visibility) {
        addNewTextView.setVisibility(visibility);
        closeScreen.setVisibility(visibility);
        addSticker.setVisibility(visibility);
        addBrushPath.setVisibility(visibility);
    }


    //touch listener
    public class ItemsTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(final View view, final MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    AppHelpers.offX = Math.round(motionEvent.getX());
                    AppHelpers.offY = Math.round(motionEvent.getY());
                    return false;

                case MotionEvent.ACTION_MOVE:
                    ClipData data = ClipData.newPlainText("Hfaw", "dawf");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                    view.startDrag(data, new View.DragShadowBuilder(view) {
                        @Override
                        public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
                            shadowSize.set(view.getWidth(), view.getHeight());
                            shadowTouchPoint.set(AppHelpers.offX, AppHelpers.offY);
                        }
                    }, view, 0);
                    view.setVisibility(View.INVISIBLE);
                    toggleMainControlVisibility(View.GONE);
                    deleteLayout.setVisibility(View.VISIBLE);
                    return false;

                case MotionEvent.ACTION_UP:
                    view.setVisibility(View.VISIBLE);
                    deleteLayout.setVisibility(View.GONE);
                    toggleMainControlVisibility(View.VISIBLE);
                    return false;
            }
            return false;
        }
    }

    //drag listener
    public class layoutDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {
            if (v.getId() == R.id.deleteItemLayout) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_ENTERED:
                        v.setScaleX(1.5f);
                        v.setScaleY(1.5f);
                        break;

                    case DragEvent.ACTION_DRAG_EXITED:
                        v.setScaleX(1f);
                        v.setScaleY(1f);
                        break;

                    case DragEvent.ACTION_DROP:
                        View view = (View) event.getLocalState();
                        container.removeView(view);
                        v.setScaleX(1f);
                        v.setScaleY(1f);
                        deleteLayout.setVisibility(View.GONE);
                        toggleMainControlVisibility(View.VISIBLE);
                        break;
                }
            } else {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ((View) event.getLocalState()).getLayoutParams();
                switch (event.getAction()) {
                    case DragEvent.ACTION_DROP:
                        View view = (View) event.getLocalState();
                        params.leftMargin = Math.round(event.getX()) - AppHelpers.offX;
                        params.topMargin = Math.round(event.getY()) - AppHelpers.offY;
                        view.setLayoutParams(params);
                        view.setVisibility(View.VISIBLE);
                        deleteLayout.setVisibility(View.GONE);
                        toggleMainControlVisibility(View.VISIBLE);
                        break;
                }
            }
            return true;
        }
    }

    private void closeKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null)
            inputManager.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

    }

    private void openKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    //back press listener
    @Override
    public void onBackPressed() {
        if (container.getVisibility() == View.GONE) {
            container.setVisibility(View.VISIBLE);
            textEditLayout.setVisibility(View.GONE);
            return;
        }
        super.onBackPressed();
    }

}

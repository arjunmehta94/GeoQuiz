package com.arjun.android.geoquiz;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class QuizActivity extends Activity {
	
	private static final String TAG = "QuizActivity";
	private static final String KEY_INDEX = "index";
	private Button mTrueButton;
	private Button mFalseButton;
	private ImageButton mNextButton;
	private ImageButton mPrevButton;
	private TextView mQuestionTextView;
    
	private TrueFalse[] mQuestionBank = new TrueFalse[] {
			new TrueFalse(R.string.question_oceans, true),
			new TrueFalse(R.string.question_mideast, true),
			new TrueFalse(R.string.question_africa, true),
			new TrueFalse(R.string.question_americas, true),
			new TrueFalse(R.string.question_asia, true),
	};
	
	private void updateQuestion(){
		//Log.d(TAG, "Updating question text for question #" + mCurrentIndex, new Exception());
		int question = mQuestionBank[mCurrentIndex].getQuestion();
		mQuestionTextView.setText(question);
	}
	
	private void checkAnswer(boolean userPressedTrue){
		boolean answerIsTrue = mQuestionBank[mCurrentIndex].isTrueQuestion();
		int messageResId = 0;
		
		if(userPressedTrue == answerIsTrue){
			messageResId = R.string.correct_toast;
			
		}
		else{
			messageResId = R.string.incorrect_toast;
		}
		
		Toast.makeText(this, messageResId,Toast.LENGTH_SHORT).show();
		
	}
	private int mCurrentIndex = 0;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");
        setContentView(R.layout.activity_quiz);
        
        mQuestionTextView = (TextView)findViewById(R.id.question_text_view);
        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
        	@Override
			public void onClick(View v) {
				//Toast.makeText(QuizActivity.this, R.string.incorrect_toast, Toast.LENGTH_SHORT).show();
        		//mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
				updateQuestion();
			}
        });
        
        
        
        mTrueButton = (Button)findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
        	
			@Override
			public void onClick(View v) {
				//Toast.makeText(QuizActivity.this, R.string.incorrect_toast, Toast.LENGTH_SHORT).show();
				checkAnswer(true);
			}
		});
        mFalseButton = (Button)findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Toast.makeText(QuizActivity.this, R.string.correct_toast, Toast.LENGTH_SHORT).show();
				checkAnswer(false);
			}
		});
        mNextButton = (ImageButton)findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
				updateQuestion();
			}
		});
        
        mPrevButton = (ImageButton)findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
        	
        	@Override
        	public void onClick(View v){
        		mCurrentIndex = (mCurrentIndex - 1) % mQuestionBank.length;
        		if(mCurrentIndex < 0){
        			mCurrentIndex = mQuestionBank.length - 1;
        					
        		}
        		updateQuestion();
        	}
        });
        if(savedInstanceState != null){
        	mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
        }
        
        updateQuestion();
    }
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		Log.i(TAG, "onSaveInstanceState");
		savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
	}
	@Override
	public void onStart(){
		super.onStart();
		Log.d(TAG, "onStart() called");
	}
	
	@Override
	public void onPause(){
		super.onPause();
		Log.d(TAG, "onPause() called");
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Log.d(TAG, "onResume() called");
	}
	
	@Override
	public void onStop(){
		super.onStop();
		Log.d(TAG, "onStop() called");
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		Log.d(TAG, "onDestroy() called");
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.quiz, menu);
        return true;
    }
    
}

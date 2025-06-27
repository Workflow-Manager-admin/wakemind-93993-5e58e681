package com.example.androidmobileapp;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * PUBLIC_INTERFACE
 * Fullscreen activity that presents challenges to dismiss the alarm
 */
public class ChallengeActivity extends AppCompatActivity {
    
    private TextView challengeTitle;
    private TextView challengeType;
    private TextView feedbackText;
    private LinearLayout mathChallengeLayout;
    private LinearLayout patternChallengeLayout;
    private TextView mathQuestion;
    private Button mathOption1, mathOption2, mathOption3;
    private GridLayout patternGrid;
    private TextView patternInstruction;
    
    private Random random = new Random();
    private boolean isMathChallenge;
    private int correctAnswer;
    private List<Integer> correctPatternSequence;
    private List<Integer> userPatternSequence;
    private Button[] patternButtons;
    
    // Challenge pools
    private final String[][] mathChallenges = {
        {"5 + 5", "10"},
        {"10 - 3", "7"},
        {"6 × 2", "12"},
        {"15 ÷ 3", "5"},
        {"8 + 4", "12"}
    };
    
    private final int[][] patternSequences = {
        {1, 2, 3},
        {1, 5, 9},
        {1, 3, 7, 9},
        {2, 4, 6, 8},
        {5, 1, 9}
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Show on lock screen and turn screen on
        setupLockScreenDisplay();
        
        setContentView(R.layout.activity_challenge);
        
        initializeViews();
        setupChallenge();
    }
    
    /**
     * Setup to show activity on lock screen
     */
    private void setupLockScreenDisplay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        
        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    /**
     * Initialize UI components
     */
    private void initializeViews() {
        challengeTitle = findViewById(R.id.challengeTitle);
        challengeType = findViewById(R.id.challengeType);
        feedbackText = findViewById(R.id.feedbackText);
        mathChallengeLayout = findViewById(R.id.mathChallengeLayout);
        patternChallengeLayout = findViewById(R.id.patternChallengeLayout);
        mathQuestion = findViewById(R.id.mathQuestion);
        mathOption1 = findViewById(R.id.mathOption1);
        mathOption2 = findViewById(R.id.mathOption2);
        mathOption3 = findViewById(R.id.mathOption3);
        patternGrid = findViewById(R.id.patternGrid);
        patternInstruction = findViewById(R.id.patternInstruction);
        
        // Initialize pattern buttons array
        patternButtons = new Button[9];
        patternButtons[0] = findViewById(R.id.pattern1);
        patternButtons[1] = findViewById(R.id.pattern2);
        patternButtons[2] = findViewById(R.id.pattern3);
        patternButtons[3] = findViewById(R.id.pattern4);
        patternButtons[4] = findViewById(R.id.pattern5);
        patternButtons[5] = findViewById(R.id.pattern6);
        patternButtons[6] = findViewById(R.id.pattern7);
        patternButtons[7] = findViewById(R.id.pattern8);
        patternButtons[8] = findViewById(R.id.pattern9);
    }
    
    /**
     * Setup a random challenge
     */
    private void setupChallenge() {
        // Randomly choose between math and pattern challenge
        isMathChallenge = random.nextBoolean();
        
        if (isMathChallenge) {
            setupMathChallenge();
        } else {
            setupPatternChallenge();
        }
    }
    
    /**
     * Setup math challenge
     */
    private void setupMathChallenge() {
        challengeType.setText(R.string.math_challenge);
        mathChallengeLayout.setVisibility(View.VISIBLE);
        patternChallengeLayout.setVisibility(View.GONE);
        
        // Select random math problem
        int challengeIndex = random.nextInt(mathChallenges.length);
        String question = mathChallenges[challengeIndex][0];
        correctAnswer = Integer.parseInt(mathChallenges[challengeIndex][1]);
        
        mathQuestion.setText(getString(R.string.math_question_format, question));
        
        // Generate answer options
        List<Integer> options = new ArrayList<>();
        options.add(correctAnswer);
        
        // Add two wrong answers
        while (options.size() < 3) {
            int wrongAnswer = correctAnswer + random.nextInt(10) - 5;
            if (wrongAnswer != correctAnswer && wrongAnswer > 0 && !options.contains(wrongAnswer)) {
                options.add(wrongAnswer);
            }
        }
        
        Collections.shuffle(options);
        
        mathOption1.setText(String.valueOf(options.get(0)));
        mathOption2.setText(String.valueOf(options.get(1)));
        mathOption3.setText(String.valueOf(options.get(2)));
        
        // Set click listeners
        mathOption1.setOnClickListener(v -> checkMathAnswer(options.get(0)));
        mathOption2.setOnClickListener(v -> checkMathAnswer(options.get(1)));
        mathOption3.setOnClickListener(v -> checkMathAnswer(options.get(2)));
    }
    
    /**
     * Setup pattern challenge
     */
    private void setupPatternChallenge() {
        challengeType.setText(R.string.pattern_challenge);
        mathChallengeLayout.setVisibility(View.GONE);
        patternChallengeLayout.setVisibility(View.VISIBLE);
        
        // Select random pattern
        int patternIndex = random.nextInt(patternSequences.length);
        correctPatternSequence = new ArrayList<>();
        for (int num : patternSequences[patternIndex]) {
            correctPatternSequence.add(num - 1); // Convert to 0-based index
        }
        
        userPatternSequence = new ArrayList<>();
        
        // Show the pattern briefly
        showPattern();
        
        // Set up pattern button listeners
        for (int i = 0; i < patternButtons.length; i++) {
            final int index = i;
            patternButtons[i].setText("");
            patternButtons[i].setOnClickListener(v -> onPatternButtonClick(index));
        }
    }
    
    /**
     * Show the pattern to the user briefly
     */
    private void showPattern() {
        patternInstruction.setText(R.string.remember_pattern);
        
        // Highlight pattern buttons
        for (int i = 0; i < correctPatternSequence.size(); i++) {
            final int buttonIndex = correctPatternSequence.get(i);
            final int step = i + 1;
            
            new Handler().postDelayed(() -> {
                patternButtons[buttonIndex].setBackgroundColor(getResources().getColor(R.color.accent_yellow));
                patternButtons[buttonIndex].setText(String.valueOf(step));
            }, i * 800);
            
            new Handler().postDelayed(() -> {
                patternButtons[buttonIndex].setBackgroundColor(getResources().getColor(android.R.color.transparent));
                patternButtons[buttonIndex].setText("");
            }, (i + 1) * 800);
        }
        
        // After showing pattern, allow user input
        new Handler().postDelayed(() -> {
            patternInstruction.setText(R.string.tap_pattern);
        }, correctPatternSequence.size() * 800 + 500);
    }
    
    /**
     * Handle math answer selection
     */
    private void checkMathAnswer(int selectedAnswer) {
        if (selectedAnswer == correctAnswer) {
            showSuccessFeedback();
            dismissAlarm();
        } else {
            showFailureFeedback();
        }
    }
    
    /**
     * Handle pattern button clicks
     */
    private void onPatternButtonClick(int buttonIndex) {
        userPatternSequence.add(buttonIndex);
        patternButtons[buttonIndex].setText(String.valueOf(userPatternSequence.size()));
        patternButtons[buttonIndex].setBackgroundColor(getResources().getColor(R.color.primary_blue));
        
        // Check if pattern is complete
        if (userPatternSequence.size() == correctPatternSequence.size()) {
            if (userPatternSequence.equals(correctPatternSequence)) {
                showSuccessFeedback();
                dismissAlarm();
            } else {
                showFailureFeedback();
                resetPattern();
            }
        }
    }
    
    /**
     * Reset pattern challenge
     */
    private void resetPattern() {
        userPatternSequence.clear();
        for (Button button : patternButtons) {
            button.setText("");
            button.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
        
        new Handler().postDelayed(() -> {
            showPattern();
        }, 1500);
    }
    
    /**
     * Show success feedback
     */
    private void showSuccessFeedback() {
        feedbackText.setText(R.string.correct);
        feedbackText.setTextColor(getResources().getColor(R.color.success_green));
        feedbackText.setVisibility(View.VISIBLE);
    }
    
    /**
     * Show failure feedback
     */
    private void showFailureFeedback() {
        feedbackText.setText(R.string.incorrect);
        feedbackText.setTextColor(getResources().getColor(R.color.error_red));
        feedbackText.setVisibility(View.VISIBLE);
        
        new Handler().postDelayed(() -> {
            feedbackText.setVisibility(View.GONE);
        }, 1500);
    }
    
    /**
     * Dismiss alarm and close activity
     */
    private void dismissAlarm() {
        // Stop alarm service
        AlarmService.stopAlarmService(this);
        
        // Close activity after short delay
        new Handler().postDelayed(() -> {
            finish();
        }, 1500);
    }
    
    @Override
    public void onBackPressed() {
        // Prevent back button from closing the challenge
        // User must solve the challenge to dismiss alarm
        super.onBackPressed();
        // Immediately restart the activity to prevent dismissal
        Intent intent = new Intent(this, ChallengeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}

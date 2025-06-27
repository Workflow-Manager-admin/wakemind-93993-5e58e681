package com.example.androidmobileapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * PUBLIC_INTERFACE
 * Utility class to manage and generate challenges
 */
public class Challenge {
    
    public enum ChallengeType {
        MATH, PATTERN
    }
    
    public static class MathChallenge {
        public String question;
        public int correctAnswer;
        public List<Integer> options;
        
        public MathChallenge(String question, int correctAnswer, List<Integer> options) {
            this.question = question;
            this.correctAnswer = correctAnswer;
            this.options = options;
        }
    }
    
    public static class PatternChallenge {
        public List<Integer> sequence;
        public String instruction;
        
        public PatternChallenge(List<Integer> sequence, String instruction) {
            this.sequence = sequence;
            this.instruction = instruction;
        }
    }
    
    private static final String[][] MATH_PROBLEMS = {
        {"5 + 5", "10"},
        {"10 - 3", "7"},
        {"6 × 2", "12"},
        {"15 ÷ 3", "5"},
        {"8 + 4", "12"}
    };
    
    private static final int[][] PATTERN_SEQUENCES = {
        {1, 2, 3},
        {1, 5, 9},
        {1, 3, 7, 9},
        {2, 4, 6, 8},
        {5, 1, 9}
    };
    
    private static final Random random = new Random();
    
    /**
     * Generate a random math challenge
     */
    public static MathChallenge generateMathChallenge() {
        int index = random.nextInt(MATH_PROBLEMS.length);
        String question = MATH_PROBLEMS[index][0];
        int correctAnswer = Integer.parseInt(MATH_PROBLEMS[index][1]);
        
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
        
        return new MathChallenge(question, correctAnswer, options);
    }
    
    /**
     * Generate a random pattern challenge
     */
    public static PatternChallenge generatePatternChallenge() {
        int index = random.nextInt(PATTERN_SEQUENCES.length);
        List<Integer> sequence = new ArrayList<>();
        
        for (int num : PATTERN_SEQUENCES[index]) {
            sequence.add(num - 1); // Convert to 0-based index
        }
        
        return new PatternChallenge(sequence, "Tap the pattern in order");
    }
    
    /**
     * Get a random challenge type
     */
    public static ChallengeType getRandomChallengeType() {
        return random.nextBoolean() ? ChallengeType.MATH : ChallengeType.PATTERN;
    }
}

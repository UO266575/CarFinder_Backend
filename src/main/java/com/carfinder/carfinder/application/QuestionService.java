package com.carfinder.carfinder.application;

import com.carfinder.carfinder.domain.Answer;
import com.carfinder.carfinder.domain.Question;
import com.carfinder.carfinder.domain.QuestionAdapter;
import com.carfinder.carfinder.domain.exceptions.RepositoryException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final HttpSession httpSession;

    private final QuestionAdapter questionAdapter;

    public QuestionService(HttpSession httpSession, QuestionAdapter questionAdapter) {
        this.httpSession = httpSession;
        this.questionAdapter = questionAdapter;
    }

    public List<Question> getQuestions() {
        List<Question> questions;
        try {
            questions = questionAdapter.getQuestions();
        } catch (RepositoryException re) {
            return null;
        }
        return questions;
    }

    public Question getQuestionById(String id) {
        Question question;
        try {
            question = questionAdapter.getQuestionById(id);
        } catch (RepositoryException re) {
            return null;
        }
        return question;
    }

    public boolean addQuestion(Question question) {
        try {
            questionAdapter.addQuestion(question);
        } catch (RepositoryException re) {
            return false;
        }
        return true;
    }

    public boolean updateQuestion(String id, Question question) {
        if (getQuestionById(id) == null) {
            return false;
        }
        try {
            questionAdapter.updateQuestion(id, question);
        } catch (RepositoryException re) {
            return false;
        }
        return true;
    }

    public boolean deleteQuestion(String id) {
        if (getQuestionById(id) == null) {
            return false;
        }
        try {
            questionAdapter.deleteQuestion(id);
        } catch (RepositoryException re) {
            return false;
        }
        return true;
    }

    public boolean deleteAllQuestions() {
        if (getQuestions().size() == 0) {
            return false;
        }
        try {
            questionAdapter.deleteAllQuestions();
        } catch (RepositoryException re) {
            return false;
        }
        return true;
    }

    public List<Question> retrieveFiveQuestions() {
        List<Question> questions = new ArrayList<Question>();
        Set<String> questionsShown = (Set<String>) httpSession.getAttribute("questionsShown");
        if (questionsShown == null) {
            questionsShown = new HashSet<String>();
        }
        Random random = new Random();
        int questionsShownSize = questionsShown.size();
        while (questionsShown.size() - questionsShownSize < 5) {
            if (getQuestions().size() - questionsShownSize < 5) {
                List<Question> remainQuestions = retrieveRemainQuestions(questionsShown);
                questionsShown.addAll(remainQuestions.stream().map( q -> q.id()).collect(Collectors.toSet()));
                httpSession.setAttribute("questionsShown", questionsShown);
                return remainQuestions;
            }
            String id = String.valueOf(random.nextInt(17) + 5);
            if (!questionsShown.contains(id) && getQuestionById(id) != null) {
                questions.add(getQuestionById(id));
                questionsShown.add(id);
            }
        }
        httpSession.setAttribute("questionsShown", questionsShown);
        return questions;
    }

    private List<Question> retrieveRemainQuestions(Set<String> questionsShown) {
        List<Question> allQuestions = getQuestions();
        return allQuestions
                .stream()
                .filter(question -> !questionsShown.contains(question.id()))
                .toList();
    }

    public void deleteAnswer(String id) {
        String idQuestion = id.substring(id.indexOf("q") + 1, id.indexOf("_"));
        Question question = getQuestionById(idQuestion);
        List<Answer> answers = question.answers();
        List<Answer> updated = new ArrayList<>();
        for (Answer answer: answers) {
            if(!answer.id().equals(id)){
                updated.add(answer);
            }
        }
        Question updatedQuestion = new Question(question.id(), question.type(), question.text(), updated);
        updateQuestion(idQuestion, updatedQuestion);
    }
}

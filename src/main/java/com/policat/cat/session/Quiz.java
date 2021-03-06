package com.policat.cat.session;

import com.policat.cat.configs.QuizConfig;
import com.policat.cat.entities.Domain;
import com.policat.cat.entities.Question;
import com.policat.cat.entities.QuestionResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
@Scope("session")
public class Quiz {
    private Domain domain;
    private Question currentQuestion;
    private List<Long> currentSelectedAnswers = new ArrayList<>();
    private Date questionTimeLimit;
    private Boolean completed = false;
    private List<QuestionResponse> responses = new ArrayList<>();

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public Question getCurrentQuestion() {
        return currentQuestion;
    }

    public void setCurrentQuestion(Question currentQuestion) {
        this.currentQuestion = currentQuestion;
        this.currentSelectedAnswers = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, QuizConfig.MAX_QUESTION_TIME);
        this.questionTimeLimit = calendar.getTime();
    }

    public List<Long> getCurrentSelectedAnswers() {
        //workaround for null when no answers are selected
        if (this.currentSelectedAnswers == null) {
            this.currentSelectedAnswers = new ArrayList<>();
        }
        return this.currentSelectedAnswers;
    }

    public void setCurrentSelectedAnswers(ArrayList<Long> currentSelectedAnswers) {
        this.currentSelectedAnswers = currentSelectedAnswers;
    }

    public Date getQuestionTimeLimit() {
        return questionTimeLimit;
    }

    public void setQuestionTimeLimit(Date questionTimeLimit) {
        this.questionTimeLimit = questionTimeLimit;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public List<QuestionResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<QuestionResponse> responses) {
        this.responses = responses;
    }

    public void addResponse(QuestionResponse response) {
        this.responses.add(response);
    }

    public QuestionResponse getMostRecentResponse() {
        return responses.get(responses.size() - 1);
    }
}

package com.policat.cat.controllers;

import com.policat.cat.session.AuthedUser;
import com.policat.cat.entities.*;
import com.policat.cat.repositories.DomainRepository;
import com.policat.cat.repositories.QuizResultRepository;
import com.policat.cat.services.QuizService;
import com.policat.cat.session.Quiz;
import com.policat.cat.entities.QuestionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import java.util.*;

@Controller
@RequestMapping("/quiz")
@SessionAttributes("quiz")
public class QuizController {
    @Autowired
    private QuizService quizService;

    @Autowired
    DomainRepository domainRepository;

    @Autowired
    QuizResultRepository quizResultRepository;


    @ModelAttribute("quiz")
    public Quiz getDefaultQuiz() {
        return new Quiz();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showDomainsList(Model model) {
        AuthedUser auth = (AuthedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = auth.getUser();

        model.addAttribute("domains", domainRepository.findAll());
        model.addAttribute("resolved", quizResultRepository.findByUser(user));
        return "quiz_index";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String startQuiz(@ModelAttribute("domain") Long id, Model model) {
        Domain domain = quizService.getDomainWithQuestions(id);
        if (domain == null) {
            return "quiz_index";
        }

        Quiz quiz = new Quiz();
        quiz.setDomain(domain);
        model.addAttribute(quiz);

        return "redirect:/quiz/question";
    }

    @RequestMapping(value = "/question", method = RequestMethod.GET)
    public String displayChoice(@ModelAttribute Quiz quiz, SessionStatus sessionStatus) {
        //if you get on this page directly, without starting a quiz first
        if (quiz.getDomain() == null || quiz.getCompleted()) {
            sessionStatus.setComplete();
            return "redirect:/quiz";
        }

        //replace it only if it's not already set (to prevent using page refresh in order to get a new one)
        Question currentQuestion = quiz.getCurrentQuestion();
        if (currentQuestion == null) {
            currentQuestion = quizService.chooseNextQuestion(quiz);
        }
        quiz.setCurrentQuestion(currentQuestion);

        //if there are no more questions available, end the quiz
        if (currentQuestion == null) {
            quiz.setCompleted(true);
            return "redirect:/quiz/result";
        }

        return "quiz_question";
    }

    @RequestMapping(value = "/question", method = RequestMethod.POST)
    public String processChoice(@ModelAttribute Quiz quiz, SessionStatus sessionStatus) {
        if (quiz.getDomain() == null || quiz.getCompleted()) {
            sessionStatus.setComplete();
            return "redirect:/quiz";
        }

        Question newQuestion = quizService.chooseNextQuestion(quiz);
        if (newQuestion == null) {
            quiz.setCompleted(true);
            return "redirect:/quiz/result";
        }


        Set<Option> selectedOptions = new HashSet<>();

        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        //If time limit was reached, we don't take in account the answers
        if (quiz.getQuestionTimeLimit().after(now)) {
            for (Long chosenId : quiz.getCurrentSelectedAnswers()) {
                for (Option option : quiz.getCurrentQuestion().getOptions()) {
                    if (option.getId().equals(chosenId)) {
                        selectedOptions.add(option);
                        break;
                    }
                }
            }
        }

        QuestionResponse response = new QuestionResponse(quiz.getCurrentQuestion(), selectedOptions);
        quiz.addResponse(response);

        quizService.debugLastResponse(quiz);


        //prepare to choose a new question
        quiz.setCurrentQuestion(null);

        return this.displayChoice(quiz, sessionStatus);
    }

    @RequestMapping(value = "/result", method = RequestMethod.GET)
    public String displayResults(@ModelAttribute Quiz quiz, SessionStatus sessionStatus, Model model) {
        if (quiz.getDomain() == null) {
            sessionStatus.setComplete();
            return "redirect:/quiz";
        }

        if (!quiz.getCompleted()) {
            return "redirect:/quiz/question";
        }

        Integer score = quizService.calcFinalScore(quiz);

        AuthedUser auth = (AuthedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = auth.getUser();

        Domain domain = quiz.getDomain();

        List<QuestionResponse> questionResponses = quiz.getResponses();

        QuizResult quizResult = new QuizResult(score, user, domain, questionResponses);
        quizResultRepository.save(quizResult);
        model.addAttribute(quizResult);

        return "quiz_result";
    }
}

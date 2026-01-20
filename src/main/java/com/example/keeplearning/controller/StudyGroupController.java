package com.example.keeplearning.controller;

import com.example.keeplearning.entity.StudyGroup;
import com.example.keeplearning.entity.StudyGroupMember;
import com.example.keeplearning.entity.StudyGroupMessage;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.repository.StudyGroupMemberRepository;
import com.example.keeplearning.repository.StudyGroupMessageRepository;
import com.example.keeplearning.repository.StudyGroupRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/gruppen")
public class StudyGroupController {

    private final StudyGroupRepository groupRepository;
    private final StudyGroupMemberRepository memberRepository;
    private final StudyGroupMessageRepository messageRepository;

    public StudyGroupController(
            StudyGroupRepository groupRepository,
            StudyGroupMemberRepository memberRepository,
            StudyGroupMessageRepository messageRepository
    ) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.messageRepository = messageRepository;
    }

    @GetMapping
    public String list(Model model, @AuthenticationPrincipal User user) {
        List<StudyGroup> all = groupRepository.findAll();

        Set<Long> myGroupIds = new HashSet<>();
        for (StudyGroupMember m : memberRepository.findByUser(user)) {
            myGroupIds.add(m.getGroup().getId());
        }

        model.addAttribute("allGroups", all);
        model.addAttribute("myGroupIds", myGroupIds);
        return "groups/list";
    }

    @GetMapping("/neu")
    public String createForm() {
        return "groups/create";
    }

    @PostMapping("/neu")
    public String create(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @AuthenticationPrincipal User user
    ) {
        StudyGroup g = new StudyGroup();
        g.setName(name);
        g.setDescription(description);
        g.setOwner(user);
        g = groupRepository.save(g);

        memberRepository.save(StudyGroupMember.create(g, user));
        return "redirect:/gruppen/" + g.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, @AuthenticationPrincipal User user) {
        StudyGroup g = groupRepository.findById(id).orElseThrow();
        boolean isMember = memberRepository.findByGroupAndUser(g, user).isPresent();
        boolean isOwner = g.getOwner().getId().equals(user.getId());

        model.addAttribute("group", g);
        model.addAttribute("isMember", isMember);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("members", memberRepository.findByGroup(g));
        model.addAttribute("messages", messageRepository.findByGroupOrderBySentAtAsc(g));
        return "groups/detail";
    }

    @PostMapping("/{id}/beitreten")
    public String join(@PathVariable Long id, @AuthenticationPrincipal User user) {
        StudyGroup g = groupRepository.findById(id).orElseThrow();
        memberRepository.findByGroupAndUser(g, user)
                .orElseGet(() -> memberRepository.save(StudyGroupMember.create(g, user)));
        return "redirect:/gruppen/" + id;
    }

    @PostMapping("/{id}/verlassen")
    public String leave(@PathVariable Long id, @AuthenticationPrincipal User user) {
        StudyGroup g = groupRepository.findById(id).orElseThrow();
        // Owner darf die Gruppe nicht "versehentlich" verlassen
        if (g.getOwner().getId().equals(user.getId())) {
            return "redirect:/gruppen/" + id + "?ownerCannotLeave=1";
        }
        memberRepository.findByGroupAndUser(g, user).ifPresent(memberRepository::delete);
        return "redirect:/gruppen";
    }

    @PostMapping("/{id}/mitglieder/{memberId}/entfernen")
    public String removeMember(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @AuthenticationPrincipal User user
    ) {
        StudyGroup g = groupRepository.findById(id).orElseThrow();
        if (!g.getOwner().getId().equals(user.getId())) {
            return "redirect:/gruppen/" + id;
        }
        StudyGroupMember m = memberRepository.findById(memberId).orElseThrow();
        // Owner selbst nicht entfernen
        if (!m.getUser().getId().equals(g.getOwner().getId())) {
            memberRepository.delete(m);
        }
        return "redirect:/gruppen/" + id;
    }

    @PostMapping("/{id}/chat")
    public String sendMessage(
            @PathVariable Long id,
            @RequestParam String content,
            @AuthenticationPrincipal User user
    ) {
        StudyGroup g = groupRepository.findById(id).orElseThrow();
        boolean isMember = memberRepository.findByGroupAndUser(g, user).isPresent();
        if (!isMember) {
            return "redirect:/gruppen/" + id;
        }
        if (content != null && !content.trim().isEmpty()) {
            messageRepository.save(StudyGroupMessage.create(g, user, content.trim()));
        }
        return "redirect:/gruppen/" + id + "#chat";
    }
}

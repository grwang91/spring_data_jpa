package com.example.spring_data_jpa.repository;

import com.example.spring_data_jpa.entity.Member;

import java.util.List;

public interface MemberRepositoryCustom {
    List<Member> findMemberCustom();
}

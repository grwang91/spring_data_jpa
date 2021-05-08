package com.example.spring_data_jpa.repository;

import com.example.spring_data_jpa.dto.MemberDto;
import com.example.spring_data_jpa.entity.Member;
import com.example.spring_data_jpa.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(true)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;
    @PersistenceContext
    EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("grwang");
        Member savedMember = memberRepository.save(member);

        Optional<Member> byId = memberRepository.findById(savedMember.getId());
        Member findMember = byId.get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건조회
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);

    }

    @Test
    public void findUsernameAndGreaterThan() {
        Member m1 = new Member("aaa",10);
        Member m2 = new Member("aaa",20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("aaa",15);

        assertThat(result.get(0)).isEqualTo(m2);
    }

    @Test
    public void findUsername() {
        Member m1 = new Member("aaa",10);
        Member m2 = new Member("aaa",20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("aaa",10);

        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void findMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("aaa",10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> result = memberRepository.findMemberDto();

        assertThat(result.get(0)).isEqualTo(new MemberDto(m1.getId(),m1.getUsername(),team.getName()));
    }

    @Test
    public void findByNames() {


        Member m1 = new Member("aaa",10);
        Member m2 = new Member("bbb",20);
        Member m3 = new Member("ccc",30);
        memberRepository.save(m1);

        List<Member> result = memberRepository.findByNames(Arrays.asList("aaa","bbb"));

//        assertThat(result.get(0)).isEqualTo(new MemberDto(m1.getId(),m1.getUsername(),team.getName()));
    }

    @Test
    public void returnType() {


        Member m1 = new Member("aaa",10);
        Member m2 = new Member("bbb",20);
        Member m3 = new Member("ccc",30);
        memberRepository.save(m1);

        List<Member> aaa = memberRepository.findListByUsername("aaa");
        Member findMember = memberRepository.findMemberByUsername("aaa");
        Optional<Member> optionalMember = memberRepository.findOptioinalByUsername("aaa");

        assertThat(aaa.get(0)).isEqualTo(m1);
        assertThat(findMember).isEqualTo(m1);
        assertThat(optionalMember.get()).isEqualTo(m1);

    }

    @Test
    public void paging() {
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",10));
        memberRepository.save(new Member("member3",10));
        memberRepository.save(new Member("member4",10));
        memberRepository.save(new Member("member5",10));
        memberRepository.save(new Member("member6",10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(1, 3, Sort.by(Sort.Direction.DESC, "username"));
        //when
//        Page<Member> page = memberRepository.findByAge(age,pageRequest);
        Page<Member> page = memberRepository.findByAge(age,pageRequest);

        Page<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));
        //then
        List<Member> content = page.getContent();

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(6);
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isFalse();
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    public void bulkUpdate() {
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",19));
        memberRepository.save(new Member("member3",20));
        memberRepository.save(new Member("member4",21));
        memberRepository.save(new Member("member5",40));

        int resultCount = memberRepository.bulkAgePlus(20);

        //bulk update는 영속성 context를 거치지 않고 db update하기 때문에 consistency 문제 발생 가능
        //따라서 bulk update 후에는 영속성 context를 clear시켜줌
        //아니면 memberrepository에 clearAutomatically=true 로 set
//        em.flush();
//        em.clear();

        List<Member> result = memberRepository.findByUsername("member5");
        Member member5 = result.get(0);
        System.out.println("member5 = " + member5);

        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberFetch() {

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member2",20,teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        //when
        List<Member> members = memberRepository.findAll();
//        List<Member> members = memberRepository.findMemberFetchJoin();

        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("member.team = " + member.getTeam());
        }

    }
}
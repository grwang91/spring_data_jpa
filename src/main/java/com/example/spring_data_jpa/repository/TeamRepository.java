package com.example.spring_data_jpa.repository;

import com.example.spring_data_jpa.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team,Long> {
}

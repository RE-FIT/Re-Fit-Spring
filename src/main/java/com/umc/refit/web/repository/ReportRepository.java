package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.Member;
import com.umc.refit.domain.entity.ReportMem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<ReportMem, Long> {

    Optional<ReportMem> findByReporterAndReportedMember(Member reporter, Member reportedMember);

}

package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.dto.request.IssueTypeUpdateRequest;
import com.civic_reporting.cittilenz.dto.response.IssueTypeResponse;
import com.civic_reporting.cittilenz.entity.Department;
import com.civic_reporting.cittilenz.entity.IssueType;
import com.civic_reporting.cittilenz.enums.IssuePriority;
import com.civic_reporting.cittilenz.exception.ResourceNotFoundException;
import com.civic_reporting.cittilenz.mapper.IssueTypeMapper;
import com.civic_reporting.cittilenz.repository.DepartmentRepository;
import com.civic_reporting.cittilenz.repository.IssueTypeRepository;
import com.civic_reporting.cittilenz.service.IssueTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class IssueTypeServiceImpl implements IssueTypeService {

    private final IssueTypeRepository issueTypeRepository;
    private final DepartmentRepository departmentRepository;

    // ✅ CONSTRUCTOR INJECTION (MANDATORY)
    public IssueTypeServiceImpl(IssueTypeRepository issueTypeRepository,
                                DepartmentRepository departmentRepository) {
        this.issueTypeRepository = issueTypeRepository;
        this.departmentRepository = departmentRepository;
    }

    /* ============================
       READ APIs (DTOs)
       ============================ */

    @Override
    @Transactional(readOnly = true)
    public List<IssueTypeResponse> getActiveIssueTypes() {
        return issueTypeRepository.findActiveWithDepartment()
                .stream()
                .map(IssueTypeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueTypeResponse> getActiveByDepartment(Integer departmentId) {
        return issueTypeRepository
                .findActiveByDepartmentWithDepartment(departmentId)
                .stream()
                .map(IssueTypeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public IssueTypeResponse getById(Integer id) {
        IssueType it = issueTypeRepository.findByIdWithDepartment(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("IssueType not found"));
        return IssueTypeMapper.toResponse(it);
    }

    /* ============================
       WRITE APIs (ENTITIES)
       ============================ */

    @Override
    public IssueType create(IssueType issueType) {

        if (issueTypeRepository.existsByNameIgnoreCase(issueType.getName())) {
            throw new IllegalArgumentException("Issue type already exists");
        }

        issueType.setCreatedAt(LocalDateTime.now());
        return issueTypeRepository.save(issueType);
    }

    @Override
    @Transactional
    public IssueTypeResponse update(Integer id, IssueTypeUpdateRequest req) {

        // 1️⃣ Load managed entity (session OPEN)
        IssueType it = issueTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IssueType not found"));

        // 2️⃣ Update fields safely
        if (req.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            it.setDepartment(dept);
        }

        if (req.getSlaHours() != null) {
            it.setSlaHours(req.getSlaHours());
        }

        if (req.getPriority() != null) {
            it.setPriority(IssuePriority.valueOf(req.getPriority()));
        }

        if (req.getDescription() != null) {
            it.setDescription(req.getDescription());
        }

        // 3️⃣ RETURN DTO WHILE SESSION IS OPEN
        return IssueTypeMapper.toResponse(it);
    }


    @Override
    public void changeStatus(Integer id, boolean active) {

        IssueType it = issueTypeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("IssueType not found"));

        it.setActive(active);
        issueTypeRepository.save(it);
    }
}

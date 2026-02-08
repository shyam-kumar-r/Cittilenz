package com.civic_reporting.cittilenz.service;

import com.civic_reporting.cittilenz.entity.Department;
import java.util.List;

public interface DepartmentService {

    List<Department> getAll();
    Department getById(Integer id);
}

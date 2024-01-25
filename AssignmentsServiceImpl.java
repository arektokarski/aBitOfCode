package com.ttplan.service.impl;

import com.ttplan.model.Assignment;
import com.ttplan.model.Employee;
import com.ttplan.model.Shift;
import com.ttplan.model.Vehicle;
import com.ttplan.repository.AssignmentRepository;
import com.ttplan.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class AssignmentServiceImpl implements AssignmentService {
    private final AssignmentRepository assignmentRepository;

    @Autowired
    public AssignmentServiceImpl(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    @Override
    public Assignment saveAssignment(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    @Override
    public Assignment getAssignmentById(Long assignmentId) {
        return assignmentRepository.findById(assignmentId).get();
    }

    @Override
    public Assignment updateAssignment(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    @Override
    public void deleteAssignmentById(Long assignmentId) {
        assignmentRepository.deleteById(assignmentId);
    }

    @Override
    public List<Assignment> getAssignmentByDate(LocalDate assignmentDate) {
        return assignmentRepository.findAssignmentByAssignmentDate(assignmentDate);
    }

    @Override
    public List<Assignment> getAssignmentByType(String assignmentType) {
        return assignmentRepository.findAssignmentByAssignmentType(assignmentType);
    }

    @Override
    public List<Assignment> getAssignmentsBySelectedMonth(int selectedMonth, int selectedYear) {
        List<Assignment> assignments = assignmentRepository.findAll();
        return filterAssignmentsBySelectedMonth(assignments, selectedMonth, selectedYear);
    }

    @Override
    public List<Assignment> getAssignmentsByTypeAndMonth(String assignmentType, int selectedMonth, int selectedYear) {
        List<Assignment> assignmentsByType = assignmentRepository.findAssignmentByAssignmentType(assignmentType);
        return filterAssignmentsBySelectedMonth(assignmentsByType, selectedMonth, selectedYear);
    }

    @Override
    public List<Assignment> getAssignmentsByEmployeeNameAndSurnameIgnoreCase(String employeeName, String employeeSurname) {
        return assignmentRepository.findAssignmentsByEmployee_EmployeeNameIgnoreCaseAndEmployee_EmployeeSurnameIgnoreCase(employeeName, employeeSurname);
    }

    @Override
    public List<Assignment> getAssignmentsByEmployeeNameOrSurnameIgnoreCase(String employeeNameOrSurname) {
        return assignmentRepository.findAssignmentsByEmployee_EmployeeNameIgnoreCaseOrEmployee_EmployeeSurnameIgnoreCase(employeeNameOrSurname, employeeNameOrSurname);
    }

    @Override
    public List<Assignment> filterAssignmentsBySelectedMonth(List<Assignment> assignments, int selectedMonth, int selectedYear) {
        List<Assignment> filteredAssignments = new ArrayList<>();
        for (Assignment assignment : assignments) {
            LocalDate assignmentDate = assignment.getAssignmentDate();
            int assignmentMonth = assignmentDate.getMonthValue();
            int assignmentYear = assignmentDate.getYear();
            if (assignmentMonth == selectedMonth && assignmentYear == selectedYear) {
                filteredAssignments.add(assignment);
            }
        }
        return filteredAssignments;
    }

    @Override
    public List<Assignment> getAssignmentsByShiftNumberIgnoreCase(String shiftNumber) {
        List<Assignment> assignments = assignmentRepository.findAll();
        List<Assignment> filteredAssignments = new ArrayList<>();

        for (Assignment assignment : assignments) {
            if (assignment.getShift().getShiftNumber().equalsIgnoreCase(shiftNumber)) {
                filteredAssignments.add(assignment);
            }
        }

        return filteredAssignments;
    }

    @Override
    public List<Assignment> getAssignmentsByVehicleNameIgnoreCase(String vehicleName) {
        List<Assignment> assignments = assignmentRepository.findAll();
        List<Assignment> filteredAssignments = new ArrayList<>();

        for (Assignment assignment : assignments) {
            if (assignment.getVehicle().getVehicleName().equalsIgnoreCase(vehicleName)) {
                filteredAssignments.add(assignment);
            }
        }

        return filteredAssignments;
    }

    @Override
    public List<Assignment> getFilteredAssignments(Map<String, Object> filters) {
        List<Assignment> assignments = getAllAssignments();

        Integer selectedMonth = (Integer) filters.get("selectedMonth");
        Integer selectedYear = (Integer) filters.get("selectedYear");
        String assignmentType = (String) filters.get("assignmentType");
        LocalDate assignmentDate = (LocalDate) filters.get("assignmentDate");
        String employeeNameOrSurname = (String) filters.get("employeeNameOrSurname");
        String shiftNumber = (String) filters.get("shiftNumber");
        String vehicleName = (String) filters.get("vehicleName");

        if (selectedMonth != null && selectedYear != null) {
            assignments = filterAssignmentsBySelectedMonth(assignments, selectedMonth, selectedYear);
            if (assignmentType != null && !assignmentType.isEmpty()) {
                assignments = getAssignmentsByTypeAndMonth(assignmentType, selectedMonth, selectedYear);
            }
        } else if (assignmentType != null && !assignmentType.isEmpty()) {
            assignments = getAssignmentByType(assignmentType);
        }

        if (assignmentDate != null) {
            List<Assignment> assignmentsByDate = getAssignmentByDate(assignmentDate);
            assignments.retainAll(assignmentsByDate);
        }

        if (employeeNameOrSurname != null && !employeeNameOrSurname.isEmpty()) {
            assignments.retainAll(getAssignmentsByEmployeeNameOrSurnameIgnoreCase(employeeNameOrSurname));
        }

        if (shiftNumber != null && !shiftNumber.isEmpty()) {
            assignments.retainAll(getAssignmentsByShiftNumberIgnoreCase(shiftNumber));
        }
        if (vehicleName != null && !vehicleName.isEmpty()) {
            assignments.retainAll(getAssignmentsByVehicleNameIgnoreCase(vehicleName));
        }

        Collections.sort(assignments, Comparator.comparing(Assignment::getAssignmentDate));
        return assignments;
    }

    @Override
    public List<Assignment> getAssignmentsByVehicle(Vehicle vehicle) {
        return assignmentRepository.findAssignmentsByVehicle(vehicle);
    }

    @Override
    public LocalDate parseDate(String dateString) {
        List<String> dateFormats = Arrays.asList("yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy");

        for (String format : dateFormats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
            }
        }
        return null;
    }

    @Override
    public List<Assignment> getAssignmentsByEmployee(Employee employee) {
        return assignmentRepository.findAssignmentsByEmployee(employee);
    }

    @Override
    public List<Assignment> getAssignmentsByShift(Shift shift) {
        return assignmentRepository.findAssignmentByShift(shift);
    }
}
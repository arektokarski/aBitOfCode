package com.ttplan.controller;

import com.ttplan.model.Employee;
import com.ttplan.model.PlanBus;
import com.ttplan.model.SecurityUser;
import com.ttplan.service.AssignmentService;
import com.ttplan.service.EmployeeService;
import com.ttplan.service.PlanBusService;
import com.ttplan.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class PlanBusController {
    private final PlanBusService planBusService;
    private final EmployeeService employeeService;
    private final SecurityService securityService;

    @Autowired
    public PlanBusController(PlanBusService planBusService, EmployeeService employeeService, SecurityService securityService) {
        this.planBusService = planBusService;
        this.employeeService = employeeService;
        this.securityService = securityService;
    }

    @Autowired
    private AssignmentService assignmentService;

    @GetMapping("/admin/generatePlanBus")
    public String showPlanBusForm() {
        return "planBus/generatePlanBus.html";
    }

    @PostMapping("/admin/generatePlanBus")
    public String generatePlanBus(@RequestParam("selectedMonth") int selectedMonth,
                                  @RequestParam("selectedYear") int selectedYear,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        PlanBus planBus = planBusService.generatePlanBusForMonth(selectedMonth, selectedYear);
        if (planBus == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Can not generate plan as there is no assignments for selected month!");
            return "error/errorNoAssignmentsForMonth";
        }
        model.addAttribute("planBus", planBus);
        return "planBus/PlanBus.html";
    }

    @GetMapping("/user/planBus")
    public String showPlanBus(@RequestParam(value = "month", required = false) Integer month,
                              @RequestParam(value = "year", required = false) Integer year,
                              Model model) {
        if (month == null || year == null) {

            PlanBus currentPlanBus = planBusService.getCurrentPlanBus();
            model.addAttribute("planBus", currentPlanBus);
            return "planBus/planBus.html";
        }

        PlanBus planBus = planBusService.getPlanBusForMonth(month, year);
        if (planBus != null) {
            model.addAttribute("planBus", planBus);
            return "planBus/planBus.html";
        } else {

            return "error/errorNoPlanBus";
        }
    }

    @PostMapping("/user/showEmployeePlan")
    public String showEmployeePlan(@RequestParam(value = "month", required = false) Integer month,
                                   @RequestParam(value = "year", required = false) Integer year,
                                   @RequestParam("employeeSurname") String employeeSurname,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (month != null && year != null) {
            PlanBus employeePlan = planBusService.getEmployeePlanForMonthAndSurname(month, year, employeeSurname);
            if (employeePlan == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "No plan available for the selected employee and month!");
                return "error/errorNoPlanBus";
            }
            model.addAttribute("planBus", employeePlan);
            return "planBus/employeePlan.html";
        } else {
            if (employeeSurname.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Employee surname cannot be empty!");
                return "error/errorNoPlanBus";
            }
            PlanBus currentPlanBus = planBusService.getCurrentPlanBus();
            if (currentPlanBus == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "No plan available!");
                return "error/errorNoPlanBus";
            }
            PlanBus employeePlan = planBusService.getEmployeePlanForSurname(currentPlanBus, employeeSurname);
            if (employeePlan == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "No plan available for the selected employee!");
                return "error/errorNoPlanBus";
            }
            model.addAttribute("planBus", employeePlan);
            return "planBus/employeePlan.html";
        }
    }

    @GetMapping("/user/userPlanBus")
    public String getUserPlanBus(@RequestParam(value = "month", required = false) Integer month,
                                 @RequestParam(value = "year", required = false) Integer year,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        SecurityUser securityUser = securityService.getSecurityUserByUserName(username);
        if (securityUser == null) {
            return "error/errorNoUserFound";
        }
        Employee currentEmployee = securityUser.getEmployee();

        if (month != null && year != null) {
            PlanBus employeePlan = planBusService.getEmployeePlanForMonthYearNameAndSurname(month, year, currentEmployee.getEmployeeName(), currentEmployee.getEmployeeSurname());
            if (employeePlan == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "No plan available for the selected employee and month!");
                return "error/errorNoPlanBus";
            }
            model.addAttribute("planBus", employeePlan);
            return "planBus/employeePlan.html";
        } else {
            PlanBus currentPlanBus = planBusService.getEmployeePlanForMonthYearNameAndSurname(
                    LocalDate.now().getMonthValue(),
                    LocalDate.now().getYear(),
                    currentEmployee.getEmployeeName(),
                    currentEmployee.getEmployeeSurname()
            );
            if (currentPlanBus == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "No current plan available for the selected employee!");
                return "error/errorNoPlanBus";
            }
            model.addAttribute("planBus", currentPlanBus);
            return "planBus/employeePlan.html";
        }
    }
}

package com.ttplan.controller;

import com.ttplan.model.Assignment;
import com.ttplan.model.Description;
import com.ttplan.model.Shift;
import com.ttplan.service.AssignmentService;
import com.ttplan.service.DescriptionService;
import com.ttplan.service.ShiftService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/admin/shifts")
public class ShiftController {
    private ShiftService shiftService;
    private AssignmentService assignmentService;
    private DescriptionService descriptionService;

    public ShiftController(ShiftService shiftService, AssignmentService assignmentService, DescriptionService descriptionService) {
        super();
        this.shiftService = shiftService;
        this.assignmentService = assignmentService;
        this.descriptionService = descriptionService;
    }

    @GetMapping("")
    public String listShifts(Model model) {
        List<Shift> shifts = shiftService.getAllShifts();
        model.addAttribute("shifts", shifts);
        return "shift/shifts";
    }

    @GetMapping("/add")
    public String showAddShiftForm(Model model) {
        Shift shift = new Shift();
        List<Description> descriptions = descriptionService.getAllDescriptions();

        model.addAttribute("shift", shift);
        model.addAttribute("descriptions", descriptions);

        return "shift/addShift";
    }

    @PostMapping("")
    public String saveShift(@ModelAttribute("shift") @Valid Shift shift, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "shift/addShift";
        }
        if (shift.getDescription() != null) {
            List<Shift> existingShifts = shiftService.getShiftsByDescription(shift.getDescription());
            if (!existingShifts.isEmpty()) {
                return "error/errorDescriptionAssignedToShift";
            }
        }
        shift.calculateTotalTime();
        shift.calculateHoursToPay();

        Description description = shift.getDescription();
        if (description != null) {
            description = descriptionService.getDescriptionById(description.getDescriptionId());
            shift.setDescription(description);
        }

        try {
            shiftService.saveShift(shift);
        } catch (RuntimeException e) {
            return "error/error";
        }

        return "redirect:/admin/shifts";
    }


    @GetMapping("/edit/{shiftId}")
    public String editShift(@PathVariable Long shiftId, Model model) {
        Shift shift = shiftService.getShiftById(shiftId);
        List<Description> descriptions = descriptionService.getAllDescriptions();

        model.addAttribute("shift", shift);
        model.addAttribute("descriptions", descriptions);
        return "shift/editShift";
    }

    @PostMapping("/edit/{shiftId}")
    public String updateShift(@PathVariable Long shiftId, @ModelAttribute("shift") @Valid Shift shift, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "shift/editShift";
        }

        Shift existingShift = shiftService.getShiftById(shiftId);

        if (shift.getDescription() != null) {
            List<Shift> existingShifts = shiftService.getShiftsByDescription(shift.getDescription());
            existingShifts.removeIf(existing -> existing.getShiftId().equals(shiftId));
            if (!existingShifts.isEmpty()) {
                return "error/errorDescriptionAssignedToShift";
            }
        }

        existingShift.setShiftType(shift.getShiftType());
        existingShift.setShiftNumber(shift.getShiftNumber());

        Long newDescriptionId = shift.getDescription().getDescriptionId();
        if (newDescriptionId != null) {
            Description newDescription = descriptionService.getDescriptionById(newDescriptionId);
            existingShift.setDescription(newDescription);
        } else {
            existingShift.setDescription(null);
        }

        existingShift.setStartTime(shift.getStartTime());
        existingShift.setFinishTime(shift.getFinishTime());
        existingShift.setBreakTime(shift.getBreakTime());
        existingShift.setComments(shift.getComments());

        existingShift.calculateTotalTime();
        existingShift.calculateHoursToPay();

        try {
            shiftService.updateShift(existingShift);
        } catch (RuntimeException e) {
            return "error/errorShiftExists";
        } catch (Exception e) {
            return "error/error";
        }

        return "redirect:/admin/shifts";
    }


    @GetMapping("/{shiftId}")
    public String deleteShift(@PathVariable Long shiftId) {
        if (shiftId.equals(1L)) {
            return "/error/errorShiftDeletionNotAllowed";
        }
        Shift shiftToDelete = shiftService.getShiftById(shiftId);
        List<Assignment> assignmentsWithShift = assignmentService.getAssignmentsByShift(shiftToDelete);
        if (!assignmentsWithShift.isEmpty()) {
            Shift shiftToAssign = shiftService.getShiftById(1L);
            for (Assignment assignment : assignmentsWithShift) {
                assignment.setShift(shiftToAssign);
                assignmentService.saveAssignment(assignment);
            }
        }
        shiftService.deleteShiftById(shiftId);
        return "redirect:/admin/shifts";
    }

    @GetMapping("/search")
    public String searchShifts(@RequestParam("shiftNumber") String shiftNumber, Model model) {
        List<Shift> shifts = shiftService.getShiftByNumber(shiftNumber);
        model.addAttribute("shifts", shifts);
        return "shift/shifts";
    }

    @GetMapping("/detail/{shiftId}")
    public String showShiftDetail(@PathVariable Long shiftId, Model model) {
        Shift shift = shiftService.getShiftById(shiftId);
        model.addAttribute("shift", shift);
        return "shift/shiftDetail";
    }
}

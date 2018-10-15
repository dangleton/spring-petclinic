/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.appointment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 */
@Controller
class AppointmentController {

    private static final int MAX_HOUR = 17;
    private final AppointmentRepository appointments;
    private final PetRepository pets;
    private final VetRepository vets;

    public AppointmentController(AppointmentRepository visits, PetRepository pets, VetRepository vets) {
        this.appointments = visits;
        this.pets = pets;
        this.vets = vets;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @ModelAttribute("vets")
    public Collection<Vet> populateVets() {
        return this.vets.findAll();
    }

    @ModelAttribute("timeslots")
    public Collection<String> populateTimeslots() {
        List<String> timeslots = new ArrayList<>();
        // SimpleDateFormat DF = new SimpleDateFormat("HH:mm");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm a");
        LocalTime lt = LocalTime.of(8, 0);
        while (lt.getHour() < MAX_HOUR) {
            timeslots.add(lt.format(dtf));
            lt = lt.plusMinutes(30);
        }
        return timeslots;
    }

    /**
     * Called before each and every @RequestMapping annotated method. 2 goals: - Make sure we always have fresh data -
     * Since we do not use the session scope, make sure that Pet object always has an id (Even though id is not part of
     * the form fields)
     *
     * @param petId
     * @return Pet
     */
    @ModelAttribute("appointment")
    public Appointment loadPetWithVisit(@PathVariable("petId") int petId, Map<String, Object> model) {
        Pet pet = this.pets.findById(petId);
        model.put("pet", pet);
        Appointment appointment = new Appointment();
        pet.addAppointment(appointment);
        return appointment;
    }

    // Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is called
    @GetMapping("/owners/*/pets/{petId}/appointments/new")
    public String initNewApointmentForm(@PathVariable("petId") int petId, Map<String, Object> model) {
        return "pets/createOrUpdateAppointmentForm";
    }

    // Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is called
    @PostMapping("/owners/{ownerId}/pets/{petId}/appointments/new")
    public String processNewAppointmentForm(@Valid Appointment appt, BindingResult result) {
        if (!result.hasErrors()) {
            validate(result, appt);
            if (!result.hasErrors()) {
                this.appointments.save(appt);
                return "redirect:/owners/{ownerId}";
            }
        }
        // setPetInModel(appt.getPetId(), appt, result.getModel());
        return "pets/createOrUpdateAppointmentForm";

    }


    private void validate(BindingResult result, @Valid Appointment appt) {
        if (!LocalDate.now().isBefore(appt.getDate())) {
            result.addError(new FieldError("appointment", "date", "We cannot book same day appointments."));
        }
        Appointment existing = appointments.findByVetAndTimeslot(appt.getVet(), appt.getDate(), appt.getTimeslot());
        if (existing != null) {
            result.addError(new FieldError("appointment", "timeslot",
                    "Dr. " + appt.getVet() + " is already booked for this timeslot."));
        }
        existing = appointments.findByPetAndTimeslot(appt.getPetId(), appt.getDate(), appt.getTimeslot());
        if (existing != null) {
            result.addError(new FieldError("appointment", "timeslot",
                    "This pet aleady has an appointment at this date and times."));
        }
        
        if (appt.getDate().getDayOfWeek() == DayOfWeek.SATURDAY
                || appt.getDate().getDayOfWeek() == DayOfWeek.SUNDAY) {
            result.addError(new FieldError("appointment", "date", "We are not open on Saturday or Sunday."));

        }

    }

}

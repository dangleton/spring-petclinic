package org.springframework.samples.petclinic.appointment;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link VisitController}
 *
 * @author Colin But
 */
@RunWith(SpringRunner.class)
@WebMvcTest(AppointmentDeleteController.class )
public class AppointmentDeleteControllerTests {

    private static final int TEST_PET_ID = 1;
    private static final int TEST_APPOINTMENT_ID = 1;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentRepository appointments;

    @Test
    public void testDeleteAppointmentExists() throws Exception {
        Optional<Appointment> opt = Optional.of(new Appointment());
        given(this.appointments.findById(TEST_APPOINTMENT_ID)).willReturn(opt);
        mockMvc.perform(get("/owners/*/appointments/{appointmentId}/delete", TEST_PET_ID))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    public void testDeleteAppointmentNotExist() throws Exception {
        Optional<Appointment> opt = Optional.empty();
        given(this.appointments.findById(TEST_APPOINTMENT_ID)).willReturn(opt);
        mockMvc.perform(get("/owners/*/appointments/{appointmentId}/delete", TEST_PET_ID))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

}

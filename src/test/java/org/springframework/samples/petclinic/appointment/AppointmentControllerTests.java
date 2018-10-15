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
@WebMvcTest(AppointmentController.class)
public class AppointmentControllerTests {

    private static final int TEST_PET_ID = 1;
    private static final int TEST_APPOINTMENT_ID = 1;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentRepository appointments;

    @MockBean
    private PetRepository pets;
    
    @MockBean
    private VetRepository vets;

    @Before
    public void init() {
        Pet pet = new Pet();
        pet.setName("Test");
        given(this.pets.findById(TEST_PET_ID)).willReturn(pet );
        Collection<Vet> allVets = new ArrayList<Vet>();
        allVets.add(createVet("Able"));
        allVets.add(createVet("Baker"));
        given(this.vets.findAll()).willReturn(allVets);
        
    }

    private Vet createVet(String name) {
        Vet vet = new Vet();
        vet.setFirstName("first");
        vet.setLastName(name);
        return vet;
    }

    @Test
    public void testInitNewAppointmentForm() throws Exception {
        mockMvc.perform(get("/owners/*/pets/{petId}/appointments/new", TEST_PET_ID))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdateAppointmentForm"));
    }
    
   

    @Test
    public void testProcessNewAppointmentFormSuccess() throws Exception {
        LocalDate date = LocalDate.now().plusDays(1);
        if (date.getDayOfWeek().ordinal() > DayOfWeek.FRIDAY.ordinal()) {
            date = date.plusDays(2);
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        mockMvc.perform(post("/owners/*/pets/{petId}/appointments/new", TEST_PET_ID)
            .param("vet", "Baker")
            .param("date", date.format(dtf))
            .param("timeslot", "09:30 AM")
            .param("description", "Appointment Description")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    public void testProcessNewAppointmentFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/*/pets/{petId}/appointments/new", TEST_PET_ID)
            .param("name", "George")
            .flashAttr("appointment", new Appointment())
            .flashAttr("pet", new Pet())
        )
            .andExpect(model().attributeHasErrors("appointment"))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdateAppointmentForm"));
    }

}

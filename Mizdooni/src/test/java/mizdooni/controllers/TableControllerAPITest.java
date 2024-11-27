package mizdooni.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;


import mizdooni.service.RestaurantService;
import mizdooni.service.TableService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class TableControllerAPITest {

    @MockBean
    private RestaurantService restaurantService;
    @MockBean
    private TableService tableService;
    @Autowired
    private MockMvc mockMvc;



}
package mizdooni.runner;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.*;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty",
                "html:target/cucumber-reports.html",},
        glue = {"mizdooni.stepdefinitions"},
        features = {"src/test/resources/features"}

)
public class CucumberTestRunner {
}



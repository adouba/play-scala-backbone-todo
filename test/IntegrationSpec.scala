package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.ws.WS
import org.openqa.selenium._
import org.openqa.selenium.firefox._
import java.util.concurrent.TimeUnit._
import org.fluentlenium.core.filter.FilterConstructor._
import models._

object IntegrationSpec extends Specification{

  def testServer = TestServer(3333, FakeApplication(additionalConfiguration = inMemoryDatabase()))

  "Application" should {

    "run in a server" in new WithServer(app = FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      await(WS.url("http://localhost:"+port).get).status must equalTo(OK)
    }

    "work in a browser" in {
      running(testServer, classOf[FirefoxDriver]) { browser =>
        browser.goTo("http://localhost:3333/")
        browser.$("h1").first.getText must equalTo("Todos")
        Todos.findById(1).get.content must equalTo("Hello todos.")
        browser.await().atMost(5, SECONDS).until("span.edit").isPresent
        browser.$("span.edit").first.getText must equalTo("Hello todos.")
        browser.executeScript("$('a.delete').click()")
        browser.await().atMost(5, SECONDS).until("span.edit").isNotPresent;
        browser.executeScript("$('#content').val('bar')")
        browser.submit(browser.$("form"))
        browser.await().atMost(5, SECONDS).until("span.edit").isPresent
        browser.$("span.edit").first.getText must equalTo("bar")
      }
    }

  }

}
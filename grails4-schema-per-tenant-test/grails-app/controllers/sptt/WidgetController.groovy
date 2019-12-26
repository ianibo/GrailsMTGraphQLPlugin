package sptt

import grails.rest.*
import grails.converters.*
import grails.gorm.multitenancy.CurrentTenant
import grails.gorm.transactions.Transactional

@CurrentTenant
@Transactional
class WidgetController {

  def index() {
    Widget.withTransaction {
      render Widget.list() as JSON;
    }
  }

  @CurrentTenant
  def createWidget(String name) {
    def result=[status:'SURE']

    Widget.withTransaction {
      log.debug("createWidget(${params})");
      Widget w = new Widget(widgetName:name).save(flush:true, failOnError:true);
      log.debug("${Widget.list()}");
    }

    render result as JSON
  }

  @CurrentTenant
  def test() {
    log.debug("test");
    def result = [ result: 'hello' ]
    render result as JSON
  }
}

package sptt

import grails.gorm.MultiTenant
import org.grails.datastore.gorm.GormEntity

// import grails.gorm.annotation.Entity
// 
// @Entity
// class Widget implements GormEntity<Widget>, MultiTenant<Widget> {
class Widget implements MultiTenant<Widget> {

  String widgetName

    static constraints = {
    }

  public static List wibble() {
    return [
      [ 'widgetName':'wwibbee', 'hello': 'A string' ]
    ]
  }
}

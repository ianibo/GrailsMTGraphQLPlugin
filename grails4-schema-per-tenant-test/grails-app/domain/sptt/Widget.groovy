package sptt

import grails.gorm.MultiTenant
import org.grails.datastore.gorm.GormEntity
import grails.gorm.annotation.Entity

// 
// public class Widget implements MultiTenant<Widget> {
@Entity
class Widget implements GormEntity<Widget>, MultiTenant<Widget> {

  String id
  String widgetName

  public static List wibble() {
    // return [
    //   [ 'widgetName':'wwibbee', 'hello': 'A string' ]
    // ]
    return Widget.list()
  }

  static mapping = {
                   id column: 'wid_id', generator: 'uuid2', length:36
              version column: 'wid_version'
           widgetName column: 'wid_name'
  }

  static constraints = {
    widgetName(nullable:false)
  }

}

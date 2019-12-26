package sptt

import grails.gorm.MultiTenant

class WidgetLine implements MultiTenant<WidgetLine> {

  String id
  String widgetLineText
  Widget owner

  static mapping = {
                   id column: 'wl_id', generator: 'uuid2', length:36
              version column: 'wl_version'
       widgetLineText column: 'wl_txt'
                owner column: 'wl_owner_fk'
  }

  static constraints = {
    widgetLineText(nullable:true)
             owner(nullable:true)
  }

}

package org.giiwa.war.web.admin;

import org.giiwa.core.bean.Beans;
import org.giiwa.core.bean.X;
import org.giiwa.core.json.JSON;
import org.giiwa.core.bean.Helper.V;
import org.giiwa.core.bean.Helper.W;
import org.giiwa.framework.web.Model;
import org.giiwa.framework.web.Path;

public class war extends Model {

  @Path(login = true, access = "access.demo.admin")
  public void onGet() {

    this.show("/admin/demo.index.html");
  }

  @Path(path = "detail", login = true, access = "access.demo.admin")
  public void detail() {
    String id = this.getString("id");
    this.set("id", id);
    this.show("/admin/demo.detail.html");
  }

  @Path(path = "delete", login = true, access = "access.demo.admin")
  public void delete() {
    String id = this.getString("id");
    JSON jo = new JSON();
    jo.put(X.STATE, 200);
    this.response(jo);
  }

}

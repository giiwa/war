package org.giiwa.war.web.admin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.giiwa.core.base.IOUtil;
import org.giiwa.core.bean.X;
import org.giiwa.core.json.JSON;
import org.giiwa.framework.bean.Repo;
import org.giiwa.framework.bean.Repo.Entity;
import org.giiwa.framework.web.Model;
import org.giiwa.framework.web.Path;

public class war extends Model {

  private static String webapps = null;
  static {
    try {
      webapps = new File(Model.GIIWA_HOME + "/webapps/").getCanonicalPath();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @Path(login = true, access = "access.config.admin")
  public void onGet() {
    String name = this.getString("f");
    if (X.isEmpty(name)) {
      name = "/";
      this.set("root", 1);
    }
    name = webapps + name;
    File f = new File(name);
    try {
      if (!f.getCanonicalPath().startsWith(webapps)) {
        f = new File(webapps);
        this.set("root", 1);
      }
      this.set("name", name);
      this.set("list", f.listFiles());
      this.set("f", f);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    this.show("/admin/war.index.html");
  }

  public String path(File f) {
    try {
      String s1 = f.getCanonicalPath().replace(webapps, "");
      return s1;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return X.EMPTY;
  }

  @Path(path = "add", login = true, access = "access.config.admin")
  public void add() {
    JSON jo = JSON.create();
    String url = this.getString("url");
    Entity e = Repo.load(url);
    if (e != null) {
      // copy the file to webapps
      try {
        IOUtil.copy(e.getInputStream(), new FileOutputStream(webapps + "/" + e.getName()));
        jo.put(X.STATE, 200);
        jo.put(X.MESSAGE, "ok");
      } catch (Exception e1) {
        log.error(e1.getMessage(), e1);
        jo.put(X.STATE, 201);
        jo.put(X.MESSAGE, e1.getMessage());
      } finally {
        e.delete();
      }
    } else {
      jo.put(X.STATE, 201);
      jo.put(X.MESSAGE, "file not exists");
    }
    this.response(jo);
  }

  @Path(path = "detail", login = true, access = "access.config.admin")
  public void detail() {
    String id = this.getString("id");
    this.set("id", id);
    this.show("/admin/demo.detail.html");
  }

  @Path(path = "delete", login = true, access = "access.config.admin")
  public void delete() {
    JSON jo = new JSON();
    String f = this.getString("f");
    File f1 = new File(webapps + f);
    try {
      if (f1.getCanonicalPath().startsWith(webapps)) {
        IOUtil.delete(f1);
      }
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    jo.put(X.STATE, 200);
    this.response(jo);
  }

}

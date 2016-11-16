package org.giiwa.war.web.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

    String root = this.getString("root");
    String url = this.getString("url");
    Entity e = Repo.load(url);
    if (e != null) {
      // copy the file to webapps
      try {

        String name = e.getName();

        if ((name.endsWith(".war") || name.endsWith(".zip")) && (X.isEmpty(root) || X.isSame(root, "/"))) {
          // unzip
          int i = name.lastIndexOf(".");
          if (i > 0) {
            name = name.substring(0, i);
          }

          String file = webapps + root + "/" + name;
          File f = new File(file);
          if (f.exists()) {
            IOUtil.delete(f);
          }

          ZipInputStream in = new ZipInputStream(e.getInputStream());

          /**
           * store all entry in temp file
           */

          ZipEntry z = in.getNextEntry();
          byte[] bb = new byte[4 * 1024];
          while (z != null) {
            f = new File(file + "/" + z.getName());

            if (z.isDirectory()) {
              f.mkdirs();
            } else {
              if (!f.exists()) {
                f.getParentFile().mkdirs();
              }

              FileOutputStream out = new FileOutputStream(f);
              int len = in.read(bb);
              while (len > 0) {
                out.write(bb, 0, len);
                len = in.read(bb);
              }

              out.close();
            }

            z = in.getNextEntry();
          }
        } else {
          // copy the file there
          String file = webapps + root + "/" + name;
          File f = new File(file);
          if (f.exists()) {
            IOUtil.delete(f);
          }
          IOUtil.copy(e.getInputStream(), new FileOutputStream(file));
        }

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

  @Path(path = "download", login = true, access = "access.config.admin")
  public void detail() {
    String f = this.getString("f");

    File f1 = new File(webapps + f);

    String range = this.getHeader("Range");
    long total = f1.length();
    long start = 0;
    long end = total;
    if (!X.isEmpty(range)) {
      String[] s1 = X.split(range, "[=-]");
      if (s1.length > 1) {
        start = X.toLong(s1[1]);
      }

      if (s1.length > 2) {
        end = Math.min(total, X.toLong(s1[2]));

        if (end < start) {
          end = start + 16 * 1024;
        }
      }
    }

    if (end > total) {
      end = total;
    }

    long length = end - start;

    this.setContentType("application/octet");
    this.setHeader("Content-Disposition", "attachment; filename=\"" + f1.getName() + "\"");
    this.setHeader("Content-Length", Long.toString(length));
    this.setHeader("Last-Modified", lang.format(f1.lastModified(), "yyyy-MM-dd HH:mm:ss z"));
    this.setHeader("Content-Range", "bytes " + start + "-" + (end - 1) + "/" + total);
    if (start == 0) {
      this.setHeader("Accept-Ranges", "bytes");
    }
    if (end < total) {
      this.setStatus(206);
    }

    try {
      IOUtil.copy(new FileInputStream(f1), this.getOutputStream(), start, end, true);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

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

package com.source.controller;

import com.Manoj.exceptions.BadRequestException;
import com.Manoj.exceptions.DataValidationException;
import com.Manoj.exceptions.DatabaseException;
import com.Manoj.framework.AppController;
import com.Manoj.framework.utilities.messages.AppMessage;
import com.Manoj.framework.utilities.messages.BooleanMessage;
import com.Manoj.framework.utilities.messages.BooleanMessageWithDetails;
import com.Manoj.framework.utilities.messages.SimpleErrorMessage;
import com.source.dao.UsersDao;
import com.source.entities.User;
import com.source.exceptions.StorageSystemException;
import com.source.utilities.CommonUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import static org.apache.logging.log4j.web.WebLoggerContextUtils.getServletContext;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("miracle")
public class CommonController extends AppController{
    private static final String SAVE_DIR = "com.img";
    private final String UPLOAD_DIRECTORY = "com.img";
    
    @RequestMapping(value = "createUser")
    public AppMessage createUser(HttpServletRequest request , HttpServletResponse response) throws Exception{
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        UsersDao usersDao = new UsersDao(session);
        try{
            User user = usersDao.createUser(request);
            return new BooleanMessageWithDetails(user, true);
        }catch(DataValidationException ex){
            if(tx!=null){
                tx.rollback();
            }
            return  new BooleanMessage(false);
        }
    }
     
    @RequestMapping(value = "imageUpload/{userId}",headers = "content-type=multipart/*",method = RequestMethod.POST)    
    public AppMessage imageUploadHandler(@PathVariable("userId")long userId,@RequestParam("file") MultipartFile multipartFile, RedirectAttributes redirectAttributes) throws DatabaseException, BadRequestException, StorageSystemException {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        // ApplicationContext context = new ClassPathXmlApplicationContext("Beans.xml");
        UsersDao usersDao = new UsersDao(session);
        User user = (User)usersDao.read(userId);
        
        if (!multipartFile.isEmpty()) {
            String basePath = CommonUtils.getStorageFolder("Profile", 1+"");
            try {
                CommonUtils.deleteAllFilesInFolder(basePath);
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(basePath + multipartFile.getOriginalFilename())));
                FileCopyUtils.copy(multipartFile.getInputStream(), stream);
                String filepath = multipartFile.getOriginalFilename();
                user.setProfile(basePath+filepath);
                System.out.println(filepath);
                stream.close();
                
                usersDao.update(user);
                
            } catch (Exception e) {
                return new SimpleErrorMessage("There was an error in upload => " + e.getMessage());
            }
        } else {
            return new SimpleErrorMessage("No file was uploade");
        }
        
        tx.commit();
        session.close();
        return new BooleanMessageWithDetails(user,true);
    }
    
    @RequestMapping(value = "delete")
    public AppMessage delete(HttpServletRequest request,HttpServletResponse response) throws IOException, JSONException, DatabaseException{
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        UsersDao usersDao = new UsersDao(session);
        
            String jsonData = usersDao.jSonParsing(request);
            JSONObject jSONObject = new JSONObject(jsonData);
            String userId = jSONObject.getString("userId");
            
            User user = (User)usersDao.read(Long.valueOf(userId));
            if(user==null){
                throw new DatabaseException("there is no User"+userId);
                
            }
            usersDao.delete(Long.valueOf(userId));
            
            return new BooleanMessage(true);
         
    }
    
    @RequestMapping(value = "view")
    public User view(HttpServletRequest request,HttpServletResponse response) throws IOException, JSONException, DatabaseException{
       Session session = getSession();
       UsersDao usersDao = new UsersDao(session);
       String jsonData = usersDao.jSonParsing(request);
       
       JSONObject jSONObject = new JSONObject(jsonData);
       String userId = jSONObject.getString("userId");
       
       User user = (User)usersDao.read(Long.valueOf(userId));
       if(user==null){
           throw  new DatabaseException("ther is no User "+userId);
       }
       
       return user;
    }
    
    @RequestMapping(value = "verifyLogin")
    public AppMessage verifyLogin(HttpServletRequest request,HttpServletResponse response){
        Session session  =  getSession();
        UsersDao usersDao = new UsersDao(session);
        try{
            User user = usersDao.verifyLogin(request);
            return new BooleanMessageWithDetails(user,true);
        }catch(Exception ex){
            ex.printStackTrace(System.out);
            return new BooleanMessage(false);
        }
    }
}

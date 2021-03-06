package com.dorm.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dorm.pojo.Room;
import com.dorm.pojo.RoomEmployeeDetails;
import com.dorm.service.RoomEmployeeDetailsService;
import com.dorm.service.RoomService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Controller
public class RoomEmployeeDetailsController {
	
	@Autowired
	RoomEmployeeDetailsService roomEmployeeDetailsService;
	
	@Autowired
	RoomService roomService; 
	
	
	@RequestMapping(value="/getAllDetails",produces="application/json;charset=utf-8")
	@ResponseBody
	public String getAllDetails(@RequestParam(value="page",defaultValue="1")int pageNum,
			@RequestParam(value="rows",defaultValue="5")int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		List<RoomEmployeeDetails> list = roomEmployeeDetailsService.getAllDetails();
		PageInfo<RoomEmployeeDetails> p = new PageInfo<>(list);
		long total = p.getTotal();
		String json = JSON.toJSONString(p.getList(),SerializerFeature.WriteDateUseDateFormat);		
		String JSON = "{\"total\":"+total+",\"rows\":"+json+"}";
		return JSON;
	}
	
	@SuppressWarnings("null")
	@RequestMapping(value="/getDetails",produces="application/json;charset=utf-8")
	@ResponseBody
	public String getDetails(@RequestParam(value="page",defaultValue="1")int pageNum,
			@RequestParam(value="rows",defaultValue="5")int pageSize,
			@RequestParam(value="roomNo",required=false)String roomNo,
			@RequestParam(value="employeeNo",required=false)String employeeNo,
			@RequestParam(value="dateIn",required=false)String dateIn,
			@RequestParam(value="dateOut",required=false)String dateOut) throws ParseException {
		PageHelper.startPage(pageNum, pageSize);
		RoomEmployeeDetails details = new RoomEmployeeDetails();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		details.setDateIn(dateIn==null||dateIn.equals("")?null:sdf.parse(dateIn));
		details.setDateOut(dateOut==null||dateOut.equals("")?null:sdf.parse(dateOut));
		details.setEmployeeNo(employeeNo);
		details.setRoomNo(roomNo);
		List<RoomEmployeeDetails> list = roomEmployeeDetailsService.getDetails(details);
		PageInfo<RoomEmployeeDetails> p = new PageInfo<>(list);
		long total = p.getTotal();
		String json = JSON.toJSONString(p.getList(),SerializerFeature.WriteDateUseDateFormat);		
		String JSON = "{\"total\":"+total+",\"rows\":"+json+"}";
		return JSON;
	}
	
	
	
	@RequestMapping(value="/addDetails",produces="application/json;charset=utf-8")
	@ResponseBody
	public String addDetails(@RequestParam(value="roomNo")String roomNo,
			@RequestParam(value="employeeNo")String employeeNo,
			@RequestParam(value="dateIn",required=false)String dateIn) throws ParseException {
		List<RoomEmployeeDetails> list = roomEmployeeDetailsService.getLivingByEmployeeNo(employeeNo);
		if (list!=null&&list.size()>0) {
			String msg =  "判断为工号"+employeeNo+"的员工已经在住宿舍，添加失败";
			String result = "{\"success\":false,\"msg\":\""+msg+"\"}";
			return result;
		}
		List<RoomEmployeeDetails> list2 = roomEmployeeDetailsService.getLivingByRoomNo(roomNo);
		List<RoomEmployeeDetails> list3 = roomEmployeeDetailsService.getDetailsByRoomNo(roomNo);
		if ((list2!=null&&list2.size()>0)||list3.size()==0) {
			if (roomService.getRoomByRoomNo(roomNo).getSize()<=list2.size()) {				
				String msg ="判断为工号"+employeeNo+"的员工已经在住宿舍，添加失败";
				String result = "{\"success\":false,\"msg\":\""+msg+"\"}";
				return result;
			}
			RoomEmployeeDetails roomEmployeeDetails = new RoomEmployeeDetails();
			roomEmployeeDetails.setEmployeeNo(employeeNo);
			roomEmployeeDetails.setRoomNo(roomNo);
			if (dateIn==null||dateIn.equals("")) {				
				roomEmployeeDetails.setDateIn(new Date());
			}else {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				roomEmployeeDetails.setDateIn(sdf.parse(dateIn));
			}
			roomEmployeeDetailsService.addDetails(roomEmployeeDetails);
			String msg ="工号"+employeeNo+"的员工成功添加到宿舍"+roomNo+"，添加成功";
			String result = "{\"success\":true,\"msg\":\""+msg+"\"}";
			return result;
		}else {
			String msg ="判断为工号"+employeeNo+"的员工已经在住宿舍，添加失败";
			String result = "{\"success\":false,\"msg\":\""+msg+"\"}";
			return result;
		}
		
	}
	
	@RequestMapping(value="/updateDetails",produces="application/json;charset=utf-8")
	@ResponseBody
	public String updateDetails(@RequestParam(value="roomNo")String roomNo,
			@RequestParam(value="employeeNo")String employeeNo,
			@RequestParam(value="dateIn",required=false)String dateIn) throws ParseException {
		List<RoomEmployeeDetails> list = roomEmployeeDetailsService.getLivingByEmployeeNo(employeeNo);
		if (list.size()==0||list==null) {
			String msg ="调换失败，工号"+employeeNo+"的员工没有入住宿舍";
			String result = "{\"success\":false,\"msg\":\""+msg+"\"}";
			return result;
		}
		for (RoomEmployeeDetails r : list) {
			r.setDateOut(new Date());
			roomEmployeeDetailsService.updateRoom(r);
		}
		RoomEmployeeDetails r = new RoomEmployeeDetails();
		r.setDateIn(new Date());
		r.setEmployeeNo(employeeNo);
		r.setRoomNo(roomNo);
		roomEmployeeDetailsService.addDetails(r);
		String msg ="调换成功，工号"+employeeNo+"的员工已经从原宿舍"+list.get(0).getRoomNo()+"搬离至现宿舍"+roomNo+"。";
		String result = "{\"success\":true,\"msg\":\""+msg+"\"}";
		return result;
	}
	
	
	@RequestMapping(value="/leaveRoom",produces="application/json;charset=utf-8")
	@ResponseBody
	public String leaveRoom(@RequestParam(value="employeeNo")String employeeNo) {
		List<RoomEmployeeDetails> list = roomEmployeeDetailsService.getLivingByEmployeeNo(employeeNo);
		if (list.size()==0||list==null) {
			String msg ="搬离宿舍失败，工号"+employeeNo+"的员工没有入住宿舍";
			String result = "{\"success\":false,\"msg\":\""+msg+"\"}";
			return result;
		}
		for (RoomEmployeeDetails r : list) {
			r.setDateOut(new Date());
			roomEmployeeDetailsService.updateRoom(r);
		}
		String msg ="搬离成功，工号"+employeeNo+"的员工已经从宿舍"+list.get(0).getRoomNo()+"搬离。";
		String result = "{\"success\":true,\"msg\":\""+msg+"\"}";
		return result;
	}
	
	@RequestMapping(value="/deleteDetails",produces="application/json;charset=utf-8")
	@ResponseBody
	public String deleteDetails(@RequestParam(value="ids")Integer[] ids) {
		for (Integer i : ids) {
			RoomEmployeeDetails details = new RoomEmployeeDetails();
			details.setId(i);
			roomEmployeeDetailsService.deleteDetails(details);
		}
		String result = "{\"success\":true,\"msg\":\"删除成功！\"}";
		return result;
	}
	
}

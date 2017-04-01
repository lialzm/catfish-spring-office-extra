package com.catfish.support;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class MailUtil {

	@Autowired
	JavaMailSenderImpl mailSender;
	private  Logger logger = Logger.getLogger(MailUtil.class);
	
	public MailUtil() {
		logger.info("MailUtil");	
	}	

	public void sendMail(String mailTitle, String mailContent,
			String mailAddress) {
		SimpleMailMessage mailMessage=new SimpleMailMessage();
		mailMessage.setSubject(mailTitle);
		mailMessage.setText(mailContent);
		mailMessage.setTo(mailAddress);
		try {
			mailSender.send(mailMessage);
		} catch (Exception e) {
			logger.error("发送邮件失败", e);
		}
	}
}

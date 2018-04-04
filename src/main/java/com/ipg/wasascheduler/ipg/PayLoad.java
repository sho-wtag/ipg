package com.ipg.wasascheduler.ipg;

import ipgclient2.CShroff2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@ComponentScan
@EnableScheduling
public class PayLoad {

	@Autowired
	ConnectionUtil connectionUtil;

	String ReturnURL = "http://localhost:8080/ipg/response";
	String REST_SERVICE_URL = "http://ipaytest.bracbank.com:8080/ipg/servlet_pay";

	@Scheduled(fixedRate = 2000)
	public void fixedRateJob() {

		try {
			String query = "SELECT * FROM bp_transactionlog where bpt_tran_status='PROCESSING'";

			Statement st = connectionUtil.getConnection().createStatement();

			ResultSet rs = st.executeQuery(query);

			while (rs.next()) {

				repeatIteration(rs);
			}
			st.close();
		} catch (Exception e) {
			System.err.println("Got an exception! ");
			System.err.println(e.getMessage());
		}

	}

	private void repeatIteration(ResultSet rs) throws SQLException {
		if (!rs.getString("bpt_status").equals("finished")) {
			int Result = -1;
			String ErrorMessage = "";

			String EInvoice = "";
			String PTInvoice = "";

			String Action = "SaleTxnVerify";
			String MerchantID = "wasa";
			String MerRefID = rs.getString("bpt_trans_ref_id");

			PTInvoice = "<req>" + "<mer_id>" + MerchantID + "</mer_id>" + "<mer_txn_id>" + MerRefID + "</mer_txn_id>"
					+ "<action>" + Action + "</action>";

			if ((ReturnURL != null) && (ReturnURL.length() > 0)) {
				PTInvoice = PTInvoice + "<ret_url>" + ReturnURL + "/Response.jsp</ret_url>";
			}

			PTInvoice = PTInvoice + "</req>";

			CShroff2 cShroff2 = new CShroff2("C:\\merchant\\keys\\", "C:\\merchant\\logs\\");
			Result = cShroff2.getErrorCode();
			if (Result == 0) {
				Result = cShroff2.setPlainTextInvoice(PTInvoice);
				if (Result == 0) {
					EInvoice = cShroff2.getEncryptedInvoice();
				} else {
					ErrorMessage = cShroff2.getErrorMsg();
				}
			} else {
				ErrorMessage = cShroff2.getErrorMsg();
			}
			if (Result >= 0) {
				Client client;

				client = ClientBuilder.newClient();
				Form form = new Form();
				form.param("encryptedInvoicePay", EInvoice);

				client.target(REST_SERVICE_URL).request(javax.ws.rs.core.MediaType.APPLICATION_XML)
						.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);
			}
		}
	}

	@Bean
	public TaskScheduler poolScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setThreadNamePrefix("poolScheduler");
		scheduler.setPoolSize(10);
		return scheduler;
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(PayLoad.class);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			context.close();
		}
	}
}

import com.ipg.wasascheduler.ipg.ConnectionUtil;
import ipgclient2.CShroff2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.sql.PreparedStatement;

@ComponentScan
public class Response {

	@Autowired
	ConnectionUtil connectionUtil;

	private static final String SUCCESS_RESULT = "<result>success</result>";

	@POST
	@Path("/ipg/response")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String createUser(@FormParam("encryptedReceiptPay") String encryptedReceiptPay,
			@Context HttpServletResponse servletResponse) throws IOException {

		int Result = -1;
		String ErrorMessage = "";
		int StartPos = -1;
		int EndPos = -1;

		String EReceipt = "";
		String PTReceipt = "";

		String MaskedAccNo = "";
		String Action = "";
		String BankRefID = "";
		String CurrencyCode = "";
		String IPGTransactionID = "";
		String LanguageCode = "";
		String MerRefID = "";
		String MerVar1 = "";
		String MerVar2 = "";
		String MerVar3 = "";
		String MerVar4 = "";
		String CustomerName = "";
		String FailReason = "";
		String TxnAmount = "";
		String TxnStatus = "";
		EReceipt = encryptedReceiptPay;
		if (EReceipt != null) {
			if (EReceipt.length() > 0) {
				CShroff2 cShroff2 = new CShroff2("C:\\merchant\\keys\\", "C:\\merchant\\logs\\");
				Result = cShroff2.getErrorCode();
				if (Result == 0) {
					Result = cShroff2.setEncryptedReceipt(EReceipt);

					if (Result == 0) {
						PTReceipt = cShroff2.getPlainTextReceipt();

						StartPos = PTReceipt.indexOf("<acc_no>");
						EndPos = PTReceipt.indexOf("</acc_no>");

						if ((StartPos > 0) && (EndPos > StartPos)) {
							MaskedAccNo = PTReceipt.substring(StartPos + 8, EndPos);
						}

						StartPos = PTReceipt.indexOf("<action>");
						EndPos = PTReceipt.indexOf("</action>");
						if ((StartPos > 0) && (EndPos > StartPos)) {
							Action = PTReceipt.substring(StartPos + 8, EndPos);
						}

						StartPos = PTReceipt.indexOf("<bank_ref_id>");
						EndPos = PTReceipt.indexOf("</bank_ref_id>");
						if ((StartPos > 0) && (EndPos > StartPos)) {
							BankRefID = PTReceipt.substring(StartPos + 13, EndPos);
						}

						StartPos = PTReceipt.indexOf("<cur>");
						EndPos = PTReceipt.indexOf("</cur>");
						if ((StartPos > 0) && (EndPos > StartPos)) {
							CurrencyCode = PTReceipt.substring(StartPos + 5, EndPos);
						}

						StartPos = PTReceipt.indexOf("<ipg_txn_id>");
						EndPos = PTReceipt.indexOf("</ipg_txn_id>");
						if ((StartPos > 0) && (EndPos > StartPos)) {
							IPGTransactionID = PTReceipt.substring(StartPos + 12, EndPos);
						}

						StartPos = PTReceipt.indexOf("<lang>");
						EndPos = PTReceipt.indexOf("</lang>");
						if ((StartPos > 0) && (EndPos > StartPos)) {
							LanguageCode = PTReceipt.substring(StartPos + 6, EndPos);
						}

						StartPos = PTReceipt.indexOf("<mer_txn_id>");
						EndPos = PTReceipt.indexOf("</mer_txn_id>");
						if ((StartPos > 0) && (EndPos > StartPos)) {
							MerRefID = PTReceipt.substring(StartPos + 12, EndPos);
						}

						StartPos = PTReceipt.indexOf("<mer_var1>");
						EndPos = PTReceipt.indexOf("</mer_var1>");
						if ((StartPos > 0) && (EndPos > StartPos)) {
							MerVar1 = PTReceipt.substring(StartPos + 10, EndPos);
						}

						StartPos = PTReceipt.indexOf("<mer_var2>");
						EndPos = PTReceipt.indexOf("</mer_var2>");
						if ((StartPos > 0) && (EndPos > StartPos)) {
							MerVar2 = PTReceipt.substring(StartPos + 10, EndPos);
						}

						StartPos = PTReceipt.indexOf("<mer_var3>");
						EndPos = PTReceipt.indexOf("</mer_var3>");
						if ((StartPos > 0) && (EndPos > StartPos)) {
							MerVar3 = PTReceipt.substring(StartPos + 10, EndPos);
						}

						StartPos = PTReceipt.indexOf("<mer_var4>");
						EndPos = PTReceipt.indexOf("</mer_var4>");
						if ((StartPos > 0) && (EndPos > StartPos)) {
							MerVar4 = PTReceipt.substring(StartPos + 10, EndPos);
						}

						StartPos = PTReceipt.indexOf("<name>");
						EndPos = PTReceipt.indexOf("</name>");
						if ((StartPos > 0) && (EndPos > StartPos)) {
							CustomerName = PTReceipt.substring(StartPos + 6, EndPos);
						}

						StartPos = PTReceipt.indexOf("<reason>");
						EndPos = PTReceipt.indexOf("</reason>");
						if ((StartPos > 0) && (EndPos > StartPos)) {
							FailReason = PTReceipt.substring(StartPos + 8, EndPos);
						}

						StartPos = PTReceipt.indexOf("<txn_amt>");
						EndPos = PTReceipt.indexOf("</txn_amt>");
						if ((StartPos > 0) && (EndPos > StartPos)) {
							TxnAmount = PTReceipt.substring(StartPos + 9, EndPos);
						}

						StartPos = PTReceipt.indexOf("<txn_status>");
						EndPos = PTReceipt.indexOf("</txn_status>");
						if ((StartPos > 0) && (EndPos > StartPos)) {
							TxnStatus = PTReceipt.substring(StartPos + 12, EndPos);
						}
					}

					try {

						String sqlUpdate = "UPDATE bp_transactionlog " + "SET bpt_status = ? "
								+ "WHERE bpt_IPG_tran_id = ?";

						PreparedStatement pstmt = connectionUtil.getConnection().prepareStatement(sqlUpdate);

						pstmt.setString(1, TxnStatus);
						pstmt.setString(2, IPGTransactionID);
						pstmt.executeUpdate();

					} catch (Exception e) {
						System.err.println("Got an exception! ");
						System.err.println(e.getMessage());
					}

				}

				if (Result < 0) {
					ErrorMessage = cShroff2.getErrorMsg();
				}

			} else {
				ErrorMessage = "Encrypted Receipt is blank.";
			}
		} else {
			ErrorMessage = "Encrypted Receipt is NULL.";
		}

		return SUCCESS_RESULT;
	}
}

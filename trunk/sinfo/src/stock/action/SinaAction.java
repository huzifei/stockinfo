/*
 * Generated by MyEclipse Struts
 * Template path: templates/java/JavaClass.vtl
 */
package stock.action;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import stock.service.HtmlParser;
import stock.service.HttpRequest;

/** 
 * MyEclipse Struts
 * Creation date: 11-06-2009
 * Todo list:
 * 1. forwad to a new page;
 * 2. read the remote page and write into local file;
 * 3. analyze the xpath into the text and forward to the front page;
 * 4. rewrite the front page with a table?
 * 
 * XDoclet definition:
 * @struts.action path="/sina" name="sinaForm" input="/form/sinainfo.jsp" parameter="actionMethod" scope="request" validate="true"
 * @struts.action-forward name="resultok" path="resultok.jsp" contextRelative="true"
 */

/*
 * Todo:
 * 1. 计算现金流指标，增长率应该与利润增长率相近（改进函数，以通过函数获取关键词的记录）
 * 2. 计算市盈率，静态+动态，得到自己的指标
 * 3. 增加显示表格的美观
 * 
 */
public class SinaAction  extends DispatchAction {
	Logger log = Logger.getLogger(this.getClass().getName());
	final int MAXARR = 50; // 60/4 = 15year
	
	// Return variable
	String gstockTitle, gstockTillDate;
	
	Double[] gprofits = new Double[MAXARR];
	Double[] gprofitsPercent = new Double[MAXARR];
	String gprofitsAverageStr, gprofitsShortAverageStr;
	int gprofitsNum = 0;
	
	Double[] goperateCashin = new Double[MAXARR];
	Double[] goperateCashinPercent =  new Double[MAXARR];
	String goperateCashinAverageStr, goperateCashinShortAverageStr;
	int goperateCashinNum = 0;
	
	/*
	 * Input:
	 * 		stockid: 600082, 
	 * 
	 * Process:
	 * 		Get diffrent url from input $stockid
	 * Output:
	 * 		current status;
	 * 		profit: profit increase table
	 * 		
	 */
	public ActionForward test(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		
		
		// Process the input simple
		int intervalDate = 3;	// 1: 季报，当前点分析；2：半年报，06，12；3：年报，12
		final String profitStr = "净利润";
		final String operateCashflowStr = "经营活动产生的现金流量净额";
		
		int ret;
		String stockid = request.getParameter("stockid");
		log.debug("stockid = "+stockid);
		String urlProfit = "http://money.finance.sina.com.cn/corp/go.php/vFD_ProfitStatement/stockid/"+stockid+"/ctrl/part.phtml";
		String urlBalance = "http://money.finance.sina.com.cn/corp/go.php/vFD_BalanceSheet/stockid/"+stockid+"/ctrl/part.phtml";
		String urlCashflow = "http://money.finance.sina.com.cn/corp/go.php/vFD_CashFlow/stockid/"+stockid+"/ctrl/part.phtml";

		/*
		 * Profit table
		 */
		ret = profitParse(urlProfit, profitStr, intervalDate);
		if (ret < 0) {
			request.setAttribute("retResult", "Parse the profit message error");
			return mapping.findForward("actioninfo_forward");
		}
		request.setAttribute("stockTitleReq", gstockTitle);
		request.setAttribute("stockTillDateReq", gstockTillDate);
		request.setAttribute("profitsReq", gprofits);
		request.setAttribute("profitsNumReq", gprofitsNum);
		request.setAttribute("profitsPercentReq", gprofitsPercent);
		request.setAttribute("profitsAverageReq", gprofitsAverageStr);
		request.setAttribute("profitsShortAverageReq", gprofitsShortAverageStr);
		
		/*
		 * BalanceSheet
		 */
		
		/*
		 * CashSheet
		*/ 
		ret = cashflowParse(urlCashflow, operateCashflowStr, intervalDate);
		if (ret < 0) {
			request.setAttribute("retResult", "Parse the cash flow message error");
			return mapping.findForward("actioninfo_forward");
		}
		request.setAttribute("goperateCashinReq", goperateCashin);
		request.setAttribute("goperateCashinNumReq", goperateCashinNum);
		request.setAttribute("goperateCashinPercentReq", goperateCashinPercent);
		request.setAttribute("goperateCashinAverageReq", goperateCashinAverageStr);
		request.setAttribute("goperateCashinShortAverageReq", goperateCashinShortAverageStr);
		
		
		return mapping.findForward("resultok");
	}

	// Parse the profit sheet
	public int profitParse(String urlProfit, String profitStr, int intervalDate) {
		HttpRequest httpcli = new HttpRequest();
		HtmlParser htmlparser = null;
		String result = null;
				
		int numtable = 2;	// table[numtable], the num of table
		int numrow = 3;		// tr[numrow], the num of row in table, to find speical keyword
		int numcol = 2;		// td[numcol], the num of col in table, to find special date
		// Double[] gprofitsPercentData = new Double[MAXARR];
		
		/*
		 * profit
		 * 2009-09-30: 
		 * /html/body/div/div[9]/div[2]/div/div[3]/table[2]/tbody/tr/td[2], the first date row and col
		 * /html/body/div/div[9]/div[2]/div/div[3]/table[2]/tbody/tr[2]/td/a, the first keyword row and col
		 * /html/body/div/div[9]/div[2]/div/div[3]/table[2]/tbody/tr[26]/td[2],3,4,5,6
		 * 
		 * 2008-06-30: 
		 * /html/body/div/div[9]/div[2]/div/div[3]/table[3]/tbody/tr/td[2], the first date row and col
		 * /html/body/div/div[9]/div[2]/div/div[3]/table[2]/tbody/tr[2]/td/a, the first keyword row and col
		 * /html/body/div/div[9]/div[2]/div/div[3]/table[3]/tbody/tr[26]/td[2],3,4,5,6
		 * 
		 * result:
		 * all:		/html/body/div/div[9]/div[2]/div/div[3]/table[numtable]/tbody/tr[numrow]/td[numcol]
		 * date: 	/html/body/div/div[9]/div[2]/div/div[3]/table[numtable]/tbody/tr/td[numcol]
		 * keyword: /html/body/div/div[9]/div[2]/div/div[3]/table[numtable]/tbody/tr[numrow]/td/a
		 */
		httpcli.setHttpUrl(urlProfit);
		httpcli.setHttpMethod("GET");
		result = httpcli.readRemotePage();
		htmlparser = new HtmlParser(result);
		
		// Get the analyzed stock title
		String titleXpath = "/html/body/div/div[9]/div[2]/div/div/div/h1/a";
		result = htmlparser.getXpathSingleContent(titleXpath);
		gstockTitle = result;
		
		// Get the first date 
		String xpathStr = getDateXpath(numtable, numcol);
		result = htmlparser.getXpathSingleContent(xpathStr);
		String curDate = result;
		gstockTillDate= curDate;
		log.debug("stock firstDate: "+curDate);
		
		// Get the profitStr row in table, return numrow after below
		xpathStr = getKeywordXpath(numtable, numrow);
		log.debug("numtable = "+numtable+", numrow = "+numrow+"xpathStr: "+xpathStr);
		while ((result = htmlparser.getXpathSingleContent(xpathStr)) != null) {
			log.debug("result: "+result+", len = "+result.length());
			if (result.contains(profitStr) && result.length() <= 5) // 五、净利润
				break;
			numrow++;
			xpathStr = getKeywordXpath(numtable, numrow);
		}
		if (result == null) {
			log.error("ERROR: Could not get the correct profitStr = "+profitStr);
			return -1;
		}
		log.debug("find the keyword1: "+result+", with numrow = "+numrow+", result len = "+result.length());
		
		int curmonth = getDateMonth(curDate);
		String retProfit = null;
		
		for (int i=0; i<MAXARR; i++) {
			gprofits[i] = 0.0;
			gprofitsPercent[i] = 0.0;
		}
		
		int index = 0;
		Double tmpInt = 0.0;
		while (curDate != null && index < MAXARR ) {
			curDate = htmlparser.getXpathSingleContent(getDateXpath(numtable, numcol));
			if (curDate == null)	// there have no valid data now
				break;
			curmonth = getDateMonth(curDate);
			log.debug("numrow="+numtable+", numcol="+numcol+", curDate = "+curDate+", curmonth="+curmonth);
			
			if (intervalDate == 2) {	// half year
				if (curmonth == 6 || curmonth == 12) {
					retProfit = htmlparser.getXpathSingleContent(getResultXpath(numtable, numrow, numcol));
					if ((tmpInt = getDouble(retProfit)) != null) {
						gprofits[index] = tmpInt;
						if (index > 0) {
							gprofitsPercent[index-1] = getPercent(gprofits[index-1]-gprofits[index], gprofits[index]);
						}
					}
					index++;
					log.debug("result date: "+curDate+", profit: "+retProfit);
				}
			} else if (intervalDate == 3) { // whole year
				if (curmonth == 12) {
					retProfit = htmlparser.getXpathSingleContent(getResultXpath(numtable, numrow, numcol));
					if ((tmpInt = getDouble(retProfit)) != null) {
						gprofits[index] = tmpInt;
						log.debug("index = "+index+", gprofits["+index+"] = "+gprofits[index]);
						if (index > 0) {
							gprofitsPercent[index-1] = getPercent(gprofits[index-1]-gprofits[index], gprofits[index]);
							log.debug("gprofitsPercent["+(index-1)+"] ="+ gprofitsPercent[index-1] );
						}
					}
					log.debug("result date: "+curDate+", profit: "+retProfit);
					index++;
				}
			} else if (intervalDate == 1) { // season
				retProfit = htmlparser.getXpathSingleContent(getResultXpath(numtable, numrow, numcol));
				if ((tmpInt = getDouble(retProfit)) != null) {
					gprofits[index] = tmpInt;
					if (index > 0) {
						gprofitsPercent[index-1] = getPercent(gprofits[index-1]-gprofits[index], gprofits[index]);
					}
				}
				index++;
				log.debug("result date: "+curDate+", profit: "+retProfit);
			}
			
			// next
			if (numcol == 6) {
				numcol = 2;
				numtable += 1;
			} else {
				numcol += 1;
			}
		}
		// Get the average percent
		int num = 0; double sum = 0.0;
		for ( num = 0; num < index-1; num++) {
			sum += gprofitsPercent[num];
		}
		log.debug("num = "+num+", sum = "+sum);
		gprofitsNum = num;
		Double profitsAverage = 0.0;
		if (num > 0) {
			profitsAverage = sum / num;
			NumberFormat nf = NumberFormat.getInstance();
	        nf.setMinimumFractionDigits(2);
	        gprofitsAverageStr = nf.format(profitsAverage);
	        if (num > 5)
	        	num = 5;
	        else if (num > 3)
	        	num = 3;
	        else
	        	num = 0;
	        sum = 0;
	        for (int i = 0; i < num; i++) {
	        	sum += gprofitsPercent[i];
	        }
	        if (num > 0)
	        	gprofitsShortAverageStr = nf.format(sum/num);
	        else
	        	gprofitsShortAverageStr = null;
		}
		log.debug("profitsAverage = "+gprofitsAverageStr);
		
		return 0;
	}
	
	
//	 Parse the profit sheet
	public int cashflowParse(String url, String keyword, int intervalDate) {
		HttpRequest httpcli = new HttpRequest();
		HtmlParser htmlparser = null;
		String result = null;
		
		int numtable = 2;	// table[numtable], the num of table
		int numrow = 4;		// tr[numrow], the num of row in table, to find speical keyword
		int numcol = 2;		// td[numcol], the num of col in table, to find special date
		
		/*
		 * profit
		 * 2009-09-30: 
		 * /html/body/div/div[9]/div[2]/div/div[3]/table[2]/tbody/tr/td[2], the first date row and col
		 * /html/body/div/div[9]/div[2]/div/div[3]/table[2]/tbody/tr[2]/td/a, the first keyword row and col
		 * /html/body/div/div[9]/div[2]/div/div[3]/table[2]/tbody/tr[26]/td[2],3,4,5,6
		 * 
		 * 2008-06-30: 
		 * /html/body/div/div[9]/div[2]/div/div[3]/table[3]/tbody/tr/td[2], the first date row and col
		 * /html/body/div/div[9]/div[2]/div/div[3]/table[2]/tbody/tr[2]/td/a, the first keyword row and col
		 * /html/body/div/div[9]/div[2]/div/div[3]/table[3]/tbody/tr[26]/td[2],3,4,5,6
		 * 
		 * result:
		 * all:		/html/body/div/div[9]/div[2]/div/div[3]/table[numtable]/tbody/tr[numrow]/td[numcol]
		 * date: 	/html/body/div/div[9]/div[2]/div/div[3]/table[numtable]/tbody/tr/td[numcol]
		 * keyword: /html/body/div/div[9]/div[2]/div/div[3]/table[numtable]/tbody/tr[numrow]/td/a
		 */
		httpcli.setHttpUrl(url);
		httpcli.setHttpMethod("GET");
		result = httpcli.readRemotePage();
		htmlparser = new HtmlParser(result);
		
		// Get the first date 
		String xpathStr = getDateXpath(numtable, numcol);
		result = htmlparser.getXpathSingleContent(xpathStr);
		String curDate = result;
		log.debug("stock firstDate: "+curDate);
		
		// Get the profitStr row in table, return numrow after below
		xpathStr = getKeywordXpath(numtable, numrow);
		log.debug("numtable = "+numtable+", numrow = "+numrow+"xpathStr: "+xpathStr);
		while ((result = htmlparser.getXpathSingleContent(xpathStr)) != null) {
			log.debug("result: "+result+", len = "+result.length());
			if (result.contains(keyword)) 
				break;
			numrow++;
			xpathStr = getKeywordXpath(numtable, numrow);
		}
		if (result == null) {
			log.error("ERROR: Could not get the correct keyword = "+keyword);
			return -1;
		}
		log.debug("find the keyword1: "+result+", with numrow = "+numrow+", result len = "+result.length());
		
		int curmonth = getDateMonth(curDate);
		String retProfit = null;
		
		for (int i=0; i<MAXARR; i++) {
			goperateCashin[i] = 0.0;
			goperateCashinPercent[i] = 0.0;
		}
		
		int index = 0;
		Double tmpInt = 0.0;
		while (curDate != null && index < MAXARR ) {
			curDate = htmlparser.getXpathSingleContent(getDateXpath(numtable, numcol));
			log.debug("numrow="+numtable+", numcol="+numcol+", curDate = "+curDate);
			if (curDate == null)	// there have no valid data now
				break;
			curmonth = getDateMonth(curDate);
			
			if (intervalDate == 2) {	// half year
				if (curmonth == 6 || curmonth == 12) {
					retProfit = htmlparser.getXpathSingleContent(getResultXpath(numtable, numrow, numcol));
					if ((tmpInt = getDouble(retProfit)) != null) {
						goperateCashin[index] = tmpInt;
						if (index > 0) {
							goperateCashinPercent[index-1] = getPercent(goperateCashin[index-1]-goperateCashin[index], goperateCashin[index]);
						}
					}
					index++;
					log.debug("result date: "+curDate+", profit: "+retProfit);
				}
			} else if (intervalDate == 3) { // whole year
				if (curmonth == 12) {
					retProfit = htmlparser.getXpathSingleContent(getResultXpath(numtable, numrow, numcol));
					if ((tmpInt = getDouble(retProfit)) != null) {
						goperateCashin[index] = tmpInt;
						if (index > 0) {
							goperateCashinPercent[index-1] = getPercent(goperateCashin[index-1]-goperateCashin[index], goperateCashin[index]);
							log.debug("goperateCashinPercent = "+goperateCashinPercent[index-1]);
						}
					}
					log.debug("result date: "+curDate+", profit: "+retProfit);
					index++;
				}
			} else if (intervalDate == 1) { // season
				retProfit = htmlparser.getXpathSingleContent(getResultXpath(numtable, numrow, numcol));
				if ((tmpInt = getDouble(retProfit)) != null) {
					gprofits[index] = tmpInt;
					if (index > 0) {
						goperateCashinPercent[index-1] = getPercent(goperateCashin[index-1]-goperateCashin[index], goperateCashin[index]);
					}
				}
				index++;
				log.debug("result date: "+curDate+", profit: "+retProfit);
			}
			
			// next
			if (numcol == 6) {
				numcol = 2;
				numtable += 1;
			} else {
				numcol += 1;
			}
		}
		// Get the average percent
		int num = 0; double sum = 0.0;
		log.debug("goperateCashinPercent[0] = "+goperateCashinPercent[0]);
		for ( num = 0; num < index-1; num++) {	// index-1: remove the last row
			sum += goperateCashinPercent[num];
		}
		goperateCashinNum = num;
		log.debug("sum = "+sum+", num = "+num);
		Double operateCashinAverage = 0.0;
		if (num > 0) {
			operateCashinAverage = sum / num;
			NumberFormat nf = NumberFormat.getInstance();
	        nf.setMinimumFractionDigits(2);
	        goperateCashinAverageStr = nf.format(operateCashinAverage);
	        if (num > 5)
	        	num = 5;
	        else if (num > 3)
	        	num = 3;
	        else
	        	num = 0;
	        sum = 0;
	        for (int i = 0; i < num; i++) {
	        	sum += goperateCashinPercent[i];
	        }
	        if (num > 0)
	        	goperateCashinShortAverageStr = nf.format(sum/num);
	        else
	        	goperateCashinShortAverageStr = null;
		}
		log.debug("goperateCashinAverageStr = "+goperateCashinAverageStr);
		
		return 0;
	}
	
	
	// get month(06) from date 2009-06-30
	public int getDateMonth(String date) {
		String strMonth = null;
		if ((date == null) || (!date.matches("[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}")))
			return -1;
		strMonth = date.substring(date.indexOf("-")+1, date.lastIndexOf("-"));
		log.debug("getDateMonth: return Month = "+strMonth);
		int intMonth = Integer.valueOf(strMonth).intValue();
		return intMonth;
	}
	
	public String getDateXpath(int numrow, int numcol) {
		String xpath = "/html/body/div/div[9]/div[2]/div/div[3]/table["+numrow+"]/tbody/tr/td["+numcol+"]";
		return xpath;
	}
	public String getResultXpath(int numtable, int numrow, int numcol) {
		String xpath = "/html/body/div/div[9]/div[2]/div/div[3]/table["+numtable+"]/tbody/tr["+numrow+"]/td["+numcol+"]";
		return xpath;
	}
	// Get the keyword numrow
	// Cash 			/html/body/div/div[9]/div[2]/div/div[3]/table[2]/tbody/tr[33]/td/a
	public String getKeywordXpath(int numtable, int numrow) {
		String xpath = "/html/body/div/div[9]/div[2]/div/div[3]/table["+numtable+"]/tbody/tr["+numrow+"]/td/a";
		return xpath;
	}
	
	// get Interger from a String 
	public Double getDouble(String value) {
		String str = null;
		if (value == null)
			return null;
		else {
			log.debug("getDouble value = "+value);
			int val = value.indexOf(".");
			if (val > 0)
				value = value.substring(0, val);
			str = value.replace(",", "");
			str = str.replace("元", "");
		}
		log.debug("getDouble: get match value = "+str+", return double value "+Double.valueOf(str));
		if(str.matches("^[0-9]*$") || str.matches("^[-][0-9]*$")) {
			log.debug("match");
			return Double.valueOf(str);
		} else
		    return null;	
	}
	
	public Double getPercent(double p1,double p2) {
		if (p2 < 0)
			p2 = p2*(-1);
        double p3 = p1/p2;
        log.debug("p1 = "+p1+", p2 = "+p2+", p3 = "+p3);
        int i = (int)(p3*100 + 0.5);  
        double d_return = ((double)i)/100.0;  
        return d_return;    
    }
	
	public Double getPercentData1(double p1,double p2) {
        String str = "0";
        Double p3 = p1/p2;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        str = nf.format(p3);
        log.debug("getPercentData: p1 = "+p1+", p2 = "+p2+", p3 = "+p3+", percent = "+str);
        return p3;
    }
}
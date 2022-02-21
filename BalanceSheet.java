import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.time.LocalDate;
import java.time.Period;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Date;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.JsonSyntaxException;

public class BalanceSheet
{
	public static void main(String args[])
	{
		JSONParser jsonParser = new JSONParser(); // JSONParser object to parse JSON file
		try(FileReader reader = new FileReader("1-input.json"))
		{
			Object inputObject = jsonParser.parse(reader); // read file
			JSONObject inputJsonObject = (JSONObject) inputObject;
			JSONArray revenueArray = (JSONArray) inputJsonObject.get("revenueData");
			JSONArray expenseArray = (JSONArray) inputJsonObject.get("expenseData");
			List<String> startDateList = new ArrayList<String>(); // list to store all dates from the input file
			
		
			Iterator<JSONObject> revIterator = revenueArray.iterator(); // iterator to iterate through json array
			while(revIterator.hasNext())
			{
				JSONObject revobj = (JSONObject) revIterator.next();
				if(revobj.containsKey("startDate"))
				{
					Object revDateObject = revobj.get("startDate");
					String revDate = revDateObject.toString();
					String revDate1 = revDate.substring(0, 10); // extracting only the date portion from startDate
					startDateList.add(revDate1);
				}
			}
			
			Iterator<JSONObject> expIterator = expenseArray.iterator();
			while(expIterator.hasNext())
			{
				JSONObject expobj = (JSONObject) expIterator.next();
				if(expobj.containsKey("startDate"))
				{
					Object expDateObject = expobj.get("startDate");
					String expDate = expDateObject.toString();
					String expDate1 = expDate.substring(0, 10);
					startDateList.add(expDate1);
				}
			}

			/*in the revenue array and expense array, the same date can appear multiple times.
			  So we need to remove the duplicates. To achieve that, the list is converted to Set*/
			
			LinkedHashSet<String> newDateSet = new LinkedHashSet<String>(startDateList);
			List<String> newDateList = new ArrayList<String>(newDateSet);
			Collections.sort(newDateList); // newDateList is sorted in ascending order of date
			
			/*in the sorted list, the first element will be the minimum date and last element will be the maximum date*/
			
			String firstDate = newDateList.get(0);
			String lastDate = newDateList.get(newDateList.size()-1);
			LocalDate firstDateLocal = LocalDate.parse(firstDate);
			LocalDate lastDateLocal = LocalDate.parse(lastDate);
			
			/*we need to get all the dates between the minimum date and maximum date, with a gap of one month.
			  For this, datesUntil() function is used and the results are stored in a new list*/
			
			List<Object> fullDateList = firstDateLocal.datesUntil(lastDateLocal,Period.ofMonths(1)).collect(Collectors.toList());
			
			List<String> finalDateList = new ArrayList<String>();
			for(int i=0; i<fullDateList.size(); i++)
				finalDateList.add((fullDateList.get(i).toString()));
			
			finalDateList.add(lastDate);
			int balanceList[] = new int[finalDateList.size()]; //array to store balance amount for individual month

			for(int i=0; i<finalDateList.size(); i++) // initialize balance array with 0
				balanceList[i] = 0;

			/*now we need to select each date from the finalDateList and check for the presence of that date within revenue list and expense list.
			  If that date is present within revenue list, then revenue amount for that month will be added into the balance amount of that month.
			  If that date is present within expense list, then expense amount for that month will be subtracted from the balance amount of that month.
			  If that date is not present in any of the two lists, then balance amount of that month will remain zero by default.*/
			
			for(int i=0; i<finalDateList.size(); i++)
			{
				for(int j=0; j<revenueArray.size(); j++)
				{
					JSONObject revenueObject = (JSONObject) revenueArray.get(j);
					String revSdate = (revenueObject.get("startDate")).toString();
					int amt = Integer.parseInt((revenueObject.get("amount")).toString());
					if((finalDateList.get(i)).compareTo(revSdate.substring(0,10)) == 0)
						balanceList[i] += amt;
				}
				for(int j=0; j<expenseArray.size(); j++)
				{
					JSONObject expenseObject = (JSONObject) expenseArray.get(j);
					String expSdate = (expenseObject.get("startDate")).toString();
					int amt = Integer.parseInt((expenseObject.get("amount")).toString());
					if((finalDateList.get(i)).compareTo(expSdate.substring(0,10)) == 0)
						balanceList[i] -= amt;
				}
			}

			/*code to print the output in form of a JSON object*/
			JSONArray balanceArray = new JSONArray();	
			for(int i=0; i<finalDateList.size(); i++)
			{
				JSONObject balanceObject = new JSONObject();
				balanceObject.put("amount",balanceList[i]);
				balanceObject.put("startDate",(finalDateList.get(i)).concat("T00:00:00.000Z"));
				balanceArray.add(balanceObject);
			}
			System.out.println("{\n   \"balance\": [");
			for(int i=0; i< balanceArray.size()-1; i++)
			{
				JSONObject printobj = (JSONObject) balanceArray.get(i);
				System.out.println("      {");
				System.out.println("         \""+"amount"+"\""+": "+printobj.get("amount")+",");
				System.out.println("         \""+"startDate"+"\""+": "+"\""+(printobj.get("startDate")).toString()+"\"");
				System.out.println("      },");
			}
			JSONObject printobj1 = (JSONObject) balanceArray.get(balanceArray.size()-1);
			System.out.println("      {");
			System.out.println("         \""+"amount"+"\""+": "+printobj1.get("amount")+",");
			System.out.println("         \""+"startDate"+"\""+": "+"\""+(printobj1.get("startDate")).toString()+"\"");
			System.out.println("      }\n   ]\n}");
			
		} catch(FileNotFoundException e){
		  	e.printStackTrace();
		} catch(IOException e){
		  	e.printStackTrace();
		} catch(ParseException e){
		  	e.printStackTrace();		 
		} catch(JsonSyntaxException e){
		  	e.printStackTrace();
		}
	}		//end of main method
}			//end of class
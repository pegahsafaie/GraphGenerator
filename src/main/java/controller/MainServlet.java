package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.json.JSONObject;

/**
 * Servlet implementation class MainServlet
 */
@WebServlet("/upload")
@MultipartConfig
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String address;
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MainServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setAttribute("eval", "");
		request.setAttribute("firsttab", "current");
		String config = getFile("config.properties");
		address = config.split(":")[1].trim();
		
		if (request.getPart("file") != null) {
			
			request.setAttribute("eval", "");
			request.setAttribute("firsttab", "current");
			boolean profileContainsCheck = true;
			boolean removeProfilesWitoutInfo = request.getParameter("chk-removeProfilesWitoutInfo") != null
					&& request.getParameter("chk-removeProfilesWitoutInfo").toString().equals("on");
			boolean removeProfilesWithFreq1 = request.getParameter("chk-removeProfilesWithFreq1") != null
					&& request.getParameter("chk-removeProfilesWithFreq1").toString().equals("on");
			boolean removeEventsWithoutLocationAndObejct = request
					.getParameter("chk-removeEventsWithoutLocationAndObejct") != null
					&& request.getParameter("chk-removeEventsWithoutLocationAndObejct").toString().equals("on");
			boolean removeEventsWithNoCharacterObject = request
					.getParameter("chk-removeEventsWithNoCharacterObject") != null
					&& request.getParameter("chk-removeEventsWithNoCharacterObject").toString().equals("on");
			boolean coreferenceWithCoreNLP = request.getParameter("chk-coreferenceWithCoreNLP") != null
					&& request.getParameter("chk-coreferenceWithCoreNLP").toString().equals("on");
			
			boolean coreferenceWithPyCobalt = request.getParameter("chk-coreferenceWithPyCobalt") != null
					&& request.getParameter("chk-coreferenceWithPyCobalt").toString().equals("on");
			
			boolean extractPersonalityAndOrg = true;
			boolean useQuote = request.getParameter("chk-useQuote") != null
					&& request.getParameter("chk-useQuote").toString().equals("on");
			
			boolean useCoreNLPToExtractEvent = request.getParameter("chk-useCoreNLPToExtractEvent") != null
					&& request.getParameter("chk-useCoreNLPToExtractEvent").toString().equals("on");

			String mode = "story";
			if(!coreferenceWithPyCobalt && !coreferenceWithCoreNLP)//means that user does not want to do any coreference
				mode = "test";
			
			// String saveAddress = request.getServletContext().getRealPath("/classes");
			Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">
			InputStream fileContent = filePart.getInputStream();
			String fileName = filePart.getHeader("content-disposition").split(";")[2].split("=")[1];
			OutputStream out = new FileOutputStream(address + fileName);
			int read = 0;
			final byte[] bytes = new byte[1024];

			while ((read = fileContent.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}

			// BufferedReader reader = new BufferedReader(new
			// InputStreamReader(fileContent));
			// StringBuilder out = new StringBuilder();
			// String line;
			// while ((line = reader.readLine()) != null) {
			// out.append(line);
			// out.append(System.getProperty("line.separator"));
			// }
			// reader.close();

			Path ph = Paths.get(address + fileName);
			String content = new String(Files.readAllBytes(ph));

			JSONObject destObj = new JSONObject();
			try {

				List<String> pipeLine = new ArrayList<String>();
				pipeLine.add("profile");
				pipeLine.add("event");
				pipeLine.add("relation");
				pipeLine.add("temporal");
				ReadStory readStory = new ReadStory(profileContainsCheck, removeProfilesWitoutInfo,
						removeProfilesWithFreq1, removeEventsWithoutLocationAndObejct,
						removeEventsWithNoCharacterObject, extractPersonalityAndOrg, coreferenceWithCoreNLP, useQuote, useCoreNLPToExtractEvent);
				readStory.returnJsonFromString(content, address,
						mode, pipeLine);
				destObj.put("success", "true");
			} catch (Exception ex) {
				destObj.put("fail", "true");
			}

			request.setAttribute("message", "The pipeline execution is finished. You can check the charts.");
			RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp");
			dispatcher.forward(request, response);
		}else {
			String generatedProfileFile_text="";
			String generatedRelationFile_text="";
			String annotatedProfileFile_text="";
			String annotatedRelationFile_text="";
			String annotatedQuoteFile_text="";
			if(request.getParameter("evaluation_selectMode")!=null && request.getParameter("evaluation_selectMode").equals("story")) {
			
				Part generatedProfileFile = request.getPart("generatedProfileFile");
				InputStream generatedProfileFile_Content = generatedProfileFile.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(generatedProfileFile_Content));
				StringBuilder out = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
				out.append(line);
				out.append(System.getProperty("line.separator"));}
				reader.close();
				generatedProfileFile_text = out.toString(); 
				
				Part generatedRelationFile = request.getPart("generatedRelationFile");
				InputStream generatedRelationFile_Content = generatedRelationFile.getInputStream();
				reader = new BufferedReader(new InputStreamReader(generatedRelationFile_Content));
				out = new StringBuilder();
				while ((line = reader.readLine()) != null) {
				out.append(line);
				out.append(System.getProperty("line.separator"));}
				reader.close();
				generatedRelationFile_text = out.toString();
				
				
				Part annotatedProfileFile = request.getPart("annotatedProfileFile");
				InputStream annotatedProfileFile_Content = annotatedProfileFile.getInputStream();
				reader = new BufferedReader(new InputStreamReader(annotatedProfileFile_Content));
				out = new StringBuilder();
				while ((line = reader.readLine()) != null) {
				out.append(line);
				out.append(System.getProperty("line.separator"));}
				reader.close();
				annotatedProfileFile_text = out.toString();
				
				
				Part annotatedRelationFile = request.getPart("annotatedRelationFile");
				InputStream annotatedRelationFile_Content = annotatedRelationFile.getInputStream();
				reader = new BufferedReader(new InputStreamReader(annotatedRelationFile_Content));
				out = new StringBuilder();
				while ((line = reader.readLine()) != null) {
				out.append(line);
				out.append(System.getProperty("line.separator"));}
				reader.close();
				annotatedRelationFile_text = out.toString();
				
				
				Part annotatedQuoteFile = request.getPart("annotatedQuoteFile");
				InputStream annotatedQuoteFile_Content = annotatedQuoteFile.getInputStream();
				reader = new BufferedReader(new InputStreamReader(annotatedQuoteFile_Content));
				out = new StringBuilder();
				while ((line = reader.readLine()) != null) {
				out.append(line);
				out.append(System.getProperty("line.separator"));}
				reader.close();
				annotatedQuoteFile_text = out.toString();
				
				
				Evaluator eval = new Evaluator(true, annotatedRelationFile_text, annotatedProfileFile_text, generatedProfileFile_text, generatedRelationFile_text, annotatedQuoteFile_text);
				String result = eval.strongCompareResults(false);
				request.setAttribute("foo", result);
				request.setAttribute("eval", "current");
				request.setAttribute("firsttab", "");
				RequestDispatcher rd = request.getRequestDispatcher("/index.jsp"); 
				rd.forward(request, response);
				
			}else if(request.getParameter("evaluation_selectMode")!=null){
				String value = request.getParameter("evaluation_selectMode");
				annotatedProfileFile_text = address + "ground/ground_profiles_"+value+".json";
				annotatedRelationFile_text = address + "ground/ground_relations_"+value+".json";
				generatedProfileFile_text = address + "profiles_" + value+".json";
				generatedRelationFile_text = address + "relations_" + value + ".json";
				annotatedQuoteFile_text = address + "ground/ground_quotes_"+value+".json";
				Evaluator eval = new Evaluator(true, annotatedRelationFile_text, annotatedProfileFile_text, generatedProfileFile_text, generatedRelationFile_text, annotatedQuoteFile_text);
				String result = eval.strongCompareResults(true);
				request.setAttribute("foo", result);
				request.setAttribute("eval", "current");
				request.setAttribute("firsttab", "");
				RequestDispatcher rd = request.getRequestDispatcher("/index.jsp"); 
				rd.forward(request, response);
				
			}
			
			
		}

	}

	 private String getFile(String fileName) {

	        StringBuilder result = new StringBuilder("");

	        //Get file from resources folder
	        ClassLoader classLoader = getClass().getClassLoader();
	        File file = new File(classLoader.getResource(fileName).getFile());

	        try (Scanner scanner = new Scanner(file)) {

	            while (scanner.hasNextLine()) {
	                String line = scanner.nextLine();
	                result.append(line).append("\n");
	            }

	            scanner.close();

	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        return result.toString();

	    }
}

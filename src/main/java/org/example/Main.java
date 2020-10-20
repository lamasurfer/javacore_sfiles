package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

        List<Employee> employeeListTask1 = parseCSV(columnMapping, "data.csv");
        String jsonStringTask1 = listToJson(employeeListTask1);
        writeString(jsonStringTask1, "data.json");

        List<Employee> employeeListTask2 = parseXML("data.xml");
        String jsonStringTask2 = listToJson(employeeListTask2);
        writeString(jsonStringTask2, "data2.json");

        String jsonStringTask3 = readString("new_data.json");
        List<Employee> employeeListTask3 = jsonToList(jsonStringTask3);
        for (Employee employee : employeeListTask3) {
            System.out.println(employee);
        }
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> resultList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName));
             CSVReader csvReader = new CSVReader(br)) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBeanBuilder<Employee> csvToBeanBuilder = new CsvToBeanBuilder<>(csvReader);
            csvToBeanBuilder.withMappingStrategy(strategy);
            CsvToBean<Employee> csvToBean = csvToBeanBuilder.build();
            resultList = csvToBean.parse();
        } catch (IOException e) {
            System.out.println("Exception at parseCSV(...)!");
        }
        return resultList;
    }

    public static <T> String listToJson(List<T> list) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type listType = new TypeToken<List<T>>() {}.getType();
        return gson.toJson(list, listType);
    }

    private static List<Employee> parseXML(String fileName) {
        List<Employee> employeeList = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(fileName));
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String id = element.getElementsByTagName("id").item(0).getTextContent();
                    String firstName = element.getElementsByTagName("firstName").item(0).getTextContent();
                    String lastName = element.getElementsByTagName("lastName").item(0).getTextContent();
                    String country = element.getElementsByTagName("country").item(0).getTextContent();
                    String age = element.getElementsByTagName("age").item(0).getTextContent();
                    employeeList.add(new Employee(id, firstName, lastName, country, age));
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            System.out.println("Exception at parseXML(...)!");
        }
        return employeeList;
    }

    public static void writeString(String jsonString, String outputFile) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile))) {
            bufferedWriter.write(jsonString);
            bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println("Exception at writeString(...)!");
        }
    }

    private static String readString(String fileName) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String result = null;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            JsonElement json = gson.fromJson(bufferedReader, JsonElement.class);
            result = gson.toJson(json);
        } catch (IOException e) {
            System.out.println("Exception at readString(...)!");
        }
        return result;
    }

    private static List<Employee> jsonToList(String jsonString) {
        List<Employee> employeeList = new ArrayList<>();
        try {
            JSONParser jsonParser = new JSONParser();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JSONArray jsonArray = (JSONArray) jsonParser.parse(jsonString);
            for (Object jsonArrayObject : jsonArray) {
                Employee employee = gson.fromJson(String.valueOf(jsonArrayObject), Employee.class);
                employeeList.add(employee);
            }
        } catch (ParseException e) {
            System.out.println("Exception at jsonToList(...)!");
        }
        return employeeList;
    }
}

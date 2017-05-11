import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by rio on 5/1/17.
 */

public class CodeToCountry {

  public CodeToCountry() {
  }

  public static void main(String[] args) {
    File file = new File("data.txt");
    File outputFile = new File("country.txt");

    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(outputFile);

      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

      try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        for (String line; (line = br.readLine()) != null; ) {
          // process the line.

          // Split by comma
          String[] data = line.split(",");

          String countryCode = data[0].replace("\"", "");
          String countryNumber = data[1].replace(" \"", "");
          countryNumber = countryNumber.replace("\"", "");
          String countryName = data[2].replace(" \"", "\"");
          countryName = countryName.replace("\"", "");


          // First is country code, seconds is country number,
          // third is country name.

          // <string name="country_andorra_name">Andorra</string>
          //<string name="country_andorra_code">ad</string>
          // <string name="country_andorra_number">376</string>


          //countries.add(new Country(context.getString(R.string.country_thailand_code),
          //    context.getString(R.string.country_thailand_number),
          //    context.getString(R.string.country_thailand_name)));


          bw.write("countries.add(new Country(context.getString(R.string.country_");
          String lowerCase = countryName.toLowerCase();
          lowerCase = lowerCase.replace(",", "_");
          lowerCase = lowerCase.replace("&", "");
          lowerCase = lowerCase.replace("  ", "_");
          lowerCase = lowerCase.replace(" ", "_");
          lowerCase = lowerCase.replace("__", "_");
          bw.write(lowerCase + "_code),");
          bw.newLine();
          bw.write("context.getString(R.string.country_" + lowerCase + "_number),");
          bw.newLine();
          bw.write("context.getString(R.string.country_" + lowerCase + "_name)));");
          bw.newLine();
          bw.newLine();
        }
        bw.close();
        // line is not visible here.
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}

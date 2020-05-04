package Commands;

import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;

/*
*  Lisab commandide faili commandi kus selle klassinimega.
* Eesmärk sellel on see, et saaks võimalusel kergelt hakata kontrollima, kellel, mis commandid on lubatud, et näiteks adminil on rohkem commande, mida kasutada, kui teistel
* Hetkel veel eelmise rea eesmärk on realiseerimata, aga kui tekib esmane vajadus, siis saab kohe ära teha.
 */
@CommandLine.Command(name = "addCommand", mixinStandardHelpOptions = false)
public class CommandAdder implements Callable<Integer> {

    @CommandLine.Option(names = {"-c", "--command"}, description = "Commandi nimi, mille kaudu seda välja kutsutakse")
    private String commandName;

    @CommandLine.Option(names = {"-cl", "--class"}, description = "Commandi klassi nimi")
    private String commandClassName;

    @Override
    public Integer call(){
        try(PrintWriter printWriter = new PrintWriter(new FileWriter("commands.txt", true))){
            printWriter.write(commandName + " " + commandClassName + "\n");
            printWriter.flush();
            System.out.println("Töötas");


        }catch (IOException e){
            System.out.println("ERROR: Faili ei leitud"); //Teha/kasutada mõnda teist commandi error handlemiseks?!?
            return DMExitCode.SOFTWARE;
        }

        return DMExitCode.OK;
    }
}

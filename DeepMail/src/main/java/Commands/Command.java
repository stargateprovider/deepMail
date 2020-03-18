package Commands;

/**
 * See klass saab (loodetavasti :D) olema alus kõikidele commanditele, mid deepMailis kasutda saab.
 */

public abstract class Command {


    /**
     * See on niiöelda main meetod, mis kutsutakse välja commandi käivitamisel
     * @return tagastab tulemus koodi 1 - success; -1 error; 0 - ignored?
     * Esialgu tundub, et võiks küll boolean sobida returni, aga paindlikuse võttes saaks igale koodi oma tähenduse anda, nt -1 = valed argumendid, -2 - faili ei leitud, -3 connection lost jne
     */
    public abstract int task();


    /**
     * Siin toimub commandil argumentide kontroll, ega midagi liiga palju või vähe pole
     */
    public abstract boolean hasCorrectInput();

    /**
     * Error messageid võiks mitmeid olla, mõni üldine, teine täpsem jne
     */
    public abstract String errorMsg();

    public abstract String wrongInputErrorMsg();

    /**
     * See kutsutaks välja näiteks --help commandi korral, mis kirjeldab lühidalt, mis commandiga tegu on.
     */
    public abstract String toString();


    /**
     * Lühike õpetus kuidas commandi kutsuda välja.
     */
    public abstract String usage();
}

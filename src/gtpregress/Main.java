//----------------------------------------------------------------------------
// $Id$
// $Source$
//----------------------------------------------------------------------------

package gtpregress;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import gtp.*;
import utils.*;
import version.*;

//----------------------------------------------------------------------------

class Main
{
    public static void main(String[] args)
    {
        try
        {
            String options[] = {
                "config:",
                "help",
                "output:",
                "version"
            };
            Options opt = new Options(args, options);
            opt.handleConfigOption();
            if (opt.isSet("help"))
            {
                printUsage(System.out);
                return;
            }
            if (opt.isSet("version"))
            {
                System.out.println("GtpRegress " + Version.get());
                return;
            }
            String output = opt.getString("output", "");
            Vector arguments = opt.getArguments();
            int size = arguments.size();
            if (size < 2)
            {
                printUsage(System.err);
                System.exit(-1);
            }
            String program = (String)arguments.get(0);
            String tests[] = new String[size - 1];
            for (int i = 0; i <  size - 1; ++i)
                tests[i] = (String)arguments.get(i + 1);
            new GtpRegress(program, tests, output);
        }
        catch (Throwable t)
        {
            StringUtils.printException(t);
            System.exit(-1);
        }
    }

    private static void printUsage(PrintStream out)
    {
        out.print("Usage: java -jar regression.jar [options] program test.tst"
                  + " [...]\n" +
                  "\n" +
                  "-config  config file\n" +
                  "-help    display this help and exit\n" +
                  "-output  output directory\n" +
                  "-version display this help and exit\n");
    }
}
    
//----------------------------------------------------------------------------

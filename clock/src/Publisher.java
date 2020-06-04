//
// Copyright (c) ZeroC, Inc. All rights reserved.
//

import java.util.ArrayList;
import java.util.Random;

import com.zeroc.demos.IceStorm.clock.Demo.*;

public class Publisher
{
    public static void main(String[] args)
    {
        int status = 0;
        java.util.List<String> extraArgs = new java.util.ArrayList<String>();

        //
        // Try with resources block - communicator is automatically destroyed
        // at the end of this try block
        //
        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.pub", extraArgs))
        {
            communicator.getProperties().setProperty("Ice.Default.Package", "com.zeroc.demos.IceStorm.clock");
            //
            // Install shutdown hook to (also) destroy communicator during JVM shutdown.
            // This ensures the communicator gets destroyed when the user interrupts the application with Ctrl-C.
            //
            Runtime.getRuntime().addShutdownHook(new Thread(() -> communicator.destroy()));

            status = run(communicator, extraArgs.toArray(new String[extraArgs.size()]));
        }
        System.exit(status);
    }

    public static void usage()
    {
        System.out.println("Usage: [--datagram|--twoway|--oneway] [topic]");
    }

    private static int run(com.zeroc.Ice.Communicator communicator, String[] args)
    {
        String option = "None";
        String topicName = "time";
        int i;

        for(i = 0; i < args.length; ++i)
        {
            String oldoption = option;
            if(args[i].equals("--datagram"))
            {
                option = "Datagram";
            }
            else if(args[i].equals("--twoway"))
            {
                option = "Twoway";
            }
            else if(args[i].equals("--oneway"))
            {
                option = "Oneway";
            }
            else if(args[i].startsWith("--"))
            {
                usage();
                return 1;
            }
            else
            {
                topicName = args[i++];
                break;
            }

            if(!oldoption.equals(option) && !oldoption.equals("None"))
            {
                usage();
                return 1;
            }
        }

        if(i != args.length)
        {
            usage();
            return 1;
        }

        com.zeroc.IceStorm.TopicManagerPrx manager = com.zeroc.IceStorm.TopicManagerPrx.checkedCast(
            communicator.propertyToProxy("TopicManager.Proxy"));
        if(manager == null)
        {
            System.err.println("invalid proxy");
            return 1;
        }

        //
        // Retrieve the topic.
        //
        com.zeroc.IceStorm.TopicPrx topic;
        try
        {
            topic = manager.retrieve(topicName);
        }
        catch(com.zeroc.IceStorm.NoSuchTopic e)
        {
            try
            {
                topic = manager.create(topicName);
            }
            catch(com.zeroc.IceStorm.TopicExists ex)
            {
                System.err.println("temporary failure, try again.");
                return 1;
            }
        }

        //
        // Get the topic's publisher object, and create a Clock proxy with
        // the mode specified as an argument of this application.
        //
        com.zeroc.Ice.ObjectPrx publisher = topic.getPublisher();
        if(option.equals("Datagram"))
        {
            publisher = publisher.ice_datagram();
        }
        else if(option.equals("Twoway"))
        {
            // Do nothing.
        }
        else // if(oneway)
        {
            publisher = publisher.ice_oneway();
        }
        ClockPrx clock = ClockPrx.uncheckedCast(publisher);

        System.out.println("publishing tick events. Press ^C to terminate the application.");
        try
        {
        	Random ran = new Random();
        	
        	//Velocidad del viento
        	int velocidad = ran.nextInt(30);
        	
        	//Temperatura del viento
        	int temperatura = ran.nextInt(20);
        	
        	//Estado del viento
        	String estado="";
        	
            while(true)
            {
            	
            	//Variacion de Velocidad
            	int vMas = ran.nextInt(3);
            	int vMenos = ran.nextInt(3);
            	velocidad = velocidad + vMas - vMenos;
            	
            	//Variacion de Temperatura
            	int tMas = ran.nextInt(3);
            	int tMenos = ran.nextInt(3);
            	temperatura = temperatura + tMas - tMenos;
            	
            	//Estado del Viento
            	if(velocidad <= 1) {
            		estado = "Calma";
            	}else if(velocidad <= 11 ) {
            		estado = "Ligero";
            	}else if(velocidad <= 17) {
            		estado = "Moderado";
            	}else if(velocidad <= 22 ) {
            		estado = "Fresco";
            	}else if(velocidad <= 34 ) {
            		estado = "Fuerte";
            	}else if(velocidad <= 48 ) {
            		estado = "Temporal";
            	}else if(velocidad <= 55 ) {
            		estado = "Fuerte temporal";
            	}else {
            		estado = "Huracan";
            	}
            	
            	
                clock.tick("TORRE DE CONTROL: \n"
                		+ "Velocidad: "  + velocidad + " nudos. " + "Estado: " + estado + "\n"
                				+ "Temperatura: " + temperatura + "°C");

                try
                {
                    Thread.currentThread();
                    Thread.sleep(1000);
                }
                catch(java.lang.InterruptedException e)
                {
                }
            }
        }
        catch(com.zeroc.Ice.CommunicatorDestroyedException ex)
        {
            // Ctrl-C triggered shutdown hook, which destroyed communicator - we're terminating
        }

        return 0;
    }
}


import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.{Simulation3, Simulation1, Simulation4, Simulation2, Simulation5}
import Examples.BasicFirstExample
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Simulation:
  val logger = CreateLogger(classOf[Simulation])


  @main def runSimulation =
    logger.info("Constructing a cloud model...")
    Simulation1.Start();
    //Simulation2.Start();
    //Simulation3.Start();
    //Simulation4.Start();
    //Simulation5.Start();
    logger.info("Finished cloud simulation...")
    //BasicCloudSimPlusExample.Start()
    //MyFirstExample.Start()

    //ReduceExample.Start()
    //ReduceExample.start()
    //BasicFirstExample.Start()
    //PowerSimulation.Start()


  /*
    @main def runReduceExample =
      logger.info("Constructing a cloud model...")
    ReduceExample.start()
    logger.info("Finished cloud simulation...")
    */

class Simulation
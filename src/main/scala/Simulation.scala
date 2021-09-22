
import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.{BasicCloudSimPlusExample, BasicFirstExample, MyFirstExample, PowerSimulation, ReduceExample, Simulation1}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Simulation:
  val logger = CreateLogger(classOf[Simulation])


  @main def runSimulation =
    logger.info("Constructing a cloud model...")
    Simulation1.Start()
    logger.info("Finished cloud simulation...")
    //BasicCloudSimPlusExample.Start()
    //MyFirstExample.Start()
    //BasicFirstExample.start();
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
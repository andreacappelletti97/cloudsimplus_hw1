
import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.{BasicCloudSimPlusExample, MyFirstExample, ReduceExample, BasicFirstExample}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Simulation:
  val logger = CreateLogger(classOf[Simulation])


  @main def runSimulation =
    //logger.info("Constructing a cloud model...")
    //BasicCloudSimPlusExample.Start()
    //MyFirstExample.Start()
    //BasicFirstExample.start();
    //ReduceExample.Start()
    //logger.info("Finished cloud simulation...")
    //ReduceExample.start()
    BasicFirstExample.Start()

  /*
    @main def runReduceExample =
      logger.info("Constructing a cloud model...")
    ReduceExample.start()
    logger.info("Finished cloud simulation...")
    */

class Simulation
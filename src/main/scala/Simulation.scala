
import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.{PowerSimulation, Simulation1, Simulation2}
import Examples.BasicFirstExample
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Simulation:
  val logger = CreateLogger(classOf[Simulation])


  @main def runSimulation =
    logger.info("Constructing a cloud model...")
    Simulation2.Start();
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
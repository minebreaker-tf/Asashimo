package rip.deadcode.asashimo

import com.google.common.util.concurrent.ListeningExecutorService
import rip.deadcode.asashimo.manipulation.Retriever
import rip.deadcode.asashimo.manipulation.Setter
import rip.deadcode.asashimo.resultmapper.GeneralResultMapper
import javax.sql.DataSource

class AsashimoRegistry(
        val dataSourceFactory: (() -> DataSource),
        val config: AsashimoConfig,
        val defaultResultMapper: GeneralResultMapper,
        val executor: ListeningExecutorService,
        val retriever: Retriever,
        val setter: Setter
)

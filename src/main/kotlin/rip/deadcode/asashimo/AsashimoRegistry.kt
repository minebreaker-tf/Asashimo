package rip.deadcode.asashimo

import com.google.common.util.concurrent.ListeningExecutorService
import javax.sql.DataSource

class AsashimoRegistry(
        val dataSourceFactory: (() -> DataSource),
        val config: AsashimoConfig,
        val executor: ListeningExecutorService
)

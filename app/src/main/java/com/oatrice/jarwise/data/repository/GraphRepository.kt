package com.oatrice.jarwise.data.repository

import com.oatrice.jarwise.data.api.GraphApi
import com.oatrice.jarwise.data.model.GraphDataPointDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface GraphRepository {
    fun getExpenseGraphData(id: String, period: String, type: String): Flow<List<GraphDataPointDto>>
}

class GraphRepositoryImpl(
    private val api: GraphApi
) : GraphRepository {

    override fun getExpenseGraphData(id: String, period: String, type: String): Flow<List<GraphDataPointDto>> = flow {
        try {
            val response = api.getExpenseGraphData(id, period, type)
            emit(response.data)
        } catch (e: Exception) {
            // Emitting empty list or rethrowing depends on UI handling. 
            // For now, let's log and emit empty list to avoid crashing, 
            // but ideally we should propagate error or Loading/Error states.
            e.printStackTrace()
            emit(emptyList()) 
        }
    }
}

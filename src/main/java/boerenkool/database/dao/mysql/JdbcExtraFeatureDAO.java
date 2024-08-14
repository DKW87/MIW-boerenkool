package boerenkool.database.dao.mysql;

import boerenkool.business.model.ExtraFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcExtraFeatureDAO implements ExtraFeatureDAO {

    private static final Logger logger = LoggerFactory.getLogger(JdbcExtraFeatureDAO.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcExtraFeatureDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        logger.info("JdbcExtraFeatureDAO instantiated");
    }

    @Override
    public List<ExtraFeature> getAll() {
        String sql = "SELECT * FROM ExtraFeature";
        return jdbcTemplate.query(sql, new ExtraFeatureRowMapper());
    }

    @Override
    public Optional<ExtraFeature> getOneById(int id) {
        String sql = "SELECT * FROM ExtraFeature WHERE extraFeatureId = ?";
        return jdbcTemplate.query(sql, new ExtraFeatureRowMapper(), id)
                .stream()
                .findFirst();
    }

    @Override
    public boolean storeOne(ExtraFeature extraFeature) {
        if (extraFeature.getExtraFeatureId() == 0) {
            insert(extraFeature);
        } else {
            updateOne(extraFeature);
        }
        return false;
    }

    @Override
    public boolean updateOne(ExtraFeature extraFeature) {
        String sql = "UPDATE ExtraFeature SET extraFeatureName = ? WHERE extraFeatureId = ?";
        int rowsAffected = jdbcTemplate.update(sql, extraFeature.getExtraFeatureName(), extraFeature.getExtraFeatureId());
        return rowsAffected > 0;
    }

    @Override
    public boolean removeOneById(int id) {
        String sql = "DELETE FROM ExtraFeature WHERE extraFeatureId = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }

    @Override
    public Optional<ExtraFeature> findByName(String extraFeatureName) {
        String sql = "SELECT * FROM ExtraFeature WHERE extraFeatureName = ?";
        return jdbcTemplate.query(sql, new ExtraFeatureRowMapper(), extraFeatureName)
                .stream()
                .findFirst();
    }

    private void insert(ExtraFeature extraFeature) {
        String sql = "INSERT INTO ExtraFeature (extraFeatureName) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"extraFeatureId"});
            ps.setString(1, extraFeature.getExtraFeatureName());
            return ps;
        }, keyHolder);
        extraFeature.setExtraFeatureId(keyHolder.getKey().intValue());
    }

    private static class ExtraFeatureRowMapper implements RowMapper<ExtraFeature> {
        @Override
        public ExtraFeature mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            int extraFeatureId = resultSet.getInt("extraFeatureId");
            String extraFeatureName = resultSet.getString("extraFeatureName");
            return new ExtraFeature(extraFeatureId, extraFeatureName);
        }
    }
}
